package com.distribuit.datos.actor

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import com.datos.vfs.FileObject
import com.distribuit.datos.common._
import com.distribuit.datos.compression.Compression
import com.distribuit.datos.compression.Compression._
import com.distribuit.datos.message.{ WorkerSchema, _ }
import com.distribuit.datos.models._
import play.api.libs.json.JsValue

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.util.Try

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 *         Manages specific type of files in a directory
 */
class Worker(val workerSchema: WorkerSchema, val datos: ActorRef, val uniqueIdGenerator: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(15, TimeUnit.SECONDS)

  private val logger = Logging(context.system, this)

  val candidates: ListBuffer[FileObject] = new ListBuffer[FileObject]

  val runningFiles: mutable.HashMap[String, FileObject] = new mutable.HashMap[String, FileObject]

  private var status: Any = ShutDown

  override def receive: Receive = {
    case Start => status = Start
    case ShutDown => status = ShutDown
    case Refresh =>
      status match {
        case Start =>
          candidates.isEmpty && runningFiles.isEmpty match {
            case true =>
              Try(refreshFileList()) match {
                case scala.util.Failure(failureMessage) =>
                  logger.error(s"event:Failed to read from path--path:${workerSchema.candidate.path}" +
                    s"--cause:${failureMessage.toString}\n ${failureMessage.getStackTrace.mkString("\n")}")
                case _ =>
                  logger.info(s"event:Refreshed worker--name:${workerSchema.name}")
              }
            case false =>
              candidates.isEmpty match {
                case true => //Do Nothing
                case false =>
                  processNextFile()
              }
          }
        case _ =>
        //Do Nothing
      }
    case Success(transactionId) =>
      runningFiles.remove(transactionId) match {
        case Some(file) =>
          logger.info(s"event:File Successfully processed--file:$file")
          onCompletion(file, workerSchema.onCompletion)
        case None =>
          logger.error(s"event:Invalid unique Id--Id:$transactionId")
      }
    case Failure(transactionId) =>
      runningFiles.remove(transactionId) match {
        case Some(file) =>
          onCompletion(file, workerSchema.onCompletion)
          logger.error(s"event:Failed to process file--file:$file")
        case None =>
          logger.error(s"event:Invalid unique Id--Id:$transactionId")
      }
    case invalidModel =>
      logger.error(s"event:Invalid model $invalidModel")
  }

  private def processNextFile(): Unit = {
    val nextFile: FileObject = candidates.head
    candidates -= nextFile
    val transactionId: String = getId()
    datos ! DatosRequest(transactionId, nextFile, workerSchema.candidate.compressionOpt, workerSchema.outPut, workerSchema.onCompletion)
    runningFiles += (transactionId -> nextFile)
  }

  private def refreshFileList(): Unit = {
    val filePath: FileObject = DatosSettings.fileManager.resolveFile(workerSchema.candidate.path)
    val folderExists: Boolean = filePath.exists()
    val isFolder: Boolean = filePath.isFolder
    folderExists && isFolder match {
      case true =>
        val children: mutable.ArrayOps[FileObject] = filePath.getChildren
        val files: Array[FileObject] = children.filter(child => child.isFile)
        val stableFiles: Array[FileObject] = files.filter(file => (System.currentTimeMillis() - file.getContent.getLastModifiedTime) > 10000)
        val finalCandidates: Array[FileObject] = stableFiles.filter(file => {
          val name: String = file.getName.getBaseName
          (workerSchema.matches.isEmpty || workerSchema.matches.exists(matchString => name.matches(matchString))) &&
            workerSchema.notMatches.forall(nonMatchingString => !name.matches(nonMatchingString))
        })
        candidates ++= finalCandidates
      case false =>
        if (!isFolder)
          logger.error(s"event:Invalid input, not a directory--input:$filePath")
        if (!folderExists)
          logger.error(s"event:Input does not exist--input:$filePath")
    }
  }

  private def getId(): String = {
    Await.result(uniqueIdGenerator ? GenerateUniqueId, timeout.duration) match {
      case id: TransactionId => id.transactionId
      case invalidModel =>
        logger.error(s"event:Invalid model recieved from unique Id generator $invalidModel")
        "NA"
    }
  }

  private def onCompletion(fileObject: FileObject, onCompletion: OnCompletion): Unit = {
    onCompletion match {
      case Delete =>
        logger.info(s"event: Deleting file--file:$fileObject")
        fileObject.delete()
      case Move(path) =>
        val directory: FileObject = DatosSettings.fileManager.resolveFile(path)
        directory.exists() match {
          case true if directory.isFile =>
            logger.error(s"event:Destination directory is a file, deleting it--destination:$directory")
            directory.createFolder()
          case true => //Do Nothing
          case false =>
            directory.createFolder()
        }
        logger.info(s"event:Moving file--source_file:$fileObject--destination:$directory")
        val destination: FileObject =
          DatosSettings.fileManager.resolveFile(
            directory,
            fileObject.getName.getBaseName
          )
        fileObject.moveTo(destination)
    }
  }
}

object Worker {
  def createWorkerSchema(workerSchema: JsValue): WorkerSchema = {
    val name: String = (workerSchema \ "worker").as[String]
    val candidate: Candidate = getCandidate((workerSchema \ "input").as[JsValue])
    val matches: List[String] = (workerSchema \ "matches").asOpt[List[String]].getOrElse(List.empty)
    val notMatches: List[String] = (workerSchema \ "not_matches").asOpt[List[String]].getOrElse(List.empty)
    val outPut: List[Modtager] = (workerSchema \ "output").as[List[JsValue]].map(getModtager)
    val onCompletion: OnCompletion = getOnCompletion((workerSchema \ "onCompletion").asOpt[JsValue])
    WorkerSchema(name, candidate, matches, notMatches, outPut, onCompletion)
  }

  def getOnCompletion(onCompletionSchemaOpt: Option[JsValue]): OnCompletion = {
    onCompletionSchemaOpt match {
      case Some(onCompletionSchema) =>
        (onCompletionSchema \ "delete").as[Boolean] match {
          case true => Delete
          case false =>
            Move((onCompletionSchema \ "movePath").as[String])
        }
      case None => Delete
    }
  }

  def getCandidate(candidateSchema: JsValue): Candidate = {
    val path: String = (candidateSchema \ "path").as[String]
    val compression: Option[Compression] = (candidateSchema \ "compress").asOpt[Boolean].getOrElse(false) match {
      case true =>
        val compressionIdentifier: String = (candidateSchema \ "compression").as[String]
        Some(Compression.identifiers(compressionIdentifier))
      case false => None
    }
    Candidate(path, compression)
  }

  def getModtager(modtagerSchema: JsValue): Modtager = {
    val path: String = (modtagerSchema \ "path").as[String]
    val compression: Option[Compression] = (modtagerSchema \ "compress").asOpt[Boolean].getOrElse(false) match {
      case true =>
        val compressionIdentifier: String = (modtagerSchema \ "compression").as[String]
        Some(Compression.identifiers(compressionIdentifier))
      case false => None
    }
    val transformer: FileNameTransformer = (modtagerSchema \ "transformer").asOpt[String].fold({
      val transformer: FileNameTransformer = Identity
      transformer
    })(className => {
      Class.forName(className).newInstance().asInstanceOf[FileNameTransformer]
    })
    Modtager(path, compression, transformer)
  }
}
