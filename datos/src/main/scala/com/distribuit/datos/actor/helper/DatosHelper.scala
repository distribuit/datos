package com.distribuit.datos.actor.helper

import java.io.{ BufferedInputStream, InputStream, OutputStream }

import com.datos.vfs.FileObject
import com.distribuit.datos.common.DatosSettings
import com.distribuit.datos.compression.Compression
import com.distribuit.datos.compression.Compression.Compression
import com.distribuit.datos.message.Modtager
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.{ Failure, Success, Try }

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 */
object DatosHelper {

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val batchSize: Int = DatosSettings.config.getInt("datos.batch.size.mb") * 1024 * 1024

  /**
   * Transfers an input file to destinations which expects the same compression.
   */
  def directTransfer(file: FileObject, modtagers: List[Modtager], compression: Option[Compression]): Boolean = {
    val fileName: String = Compression.removeCompressionExtension(file.getName.getBaseName, compression)
    val stream: InputStream = new BufferedInputStream(file.getContent.getInputStream)
    val candidates: List[(FileObject, Option[Compression])] = getFilesToWrite(modtagers, fileName)
    val filteredCandidates: List[(FileObject, Option[Compression])] = filterExistingOnes(candidates)
    val candidateStreams: List[OutputStream] = filteredCandidates.map(candidate => {
      candidate._1.createFile()
      candidate._1.getContent.getOutputStream
    })
    Try(writeAndMove(file, stream, filteredCandidates, candidateStreams)) match {
      case scala.util.Failure(errorMessage) =>
        stream.close()
        logger.error(s"event:Failed to process file --file:$file")
        false
      case Success(result) =>
        stream.close()
        result
    }
  }

  /**
   * Transfers file from one location to another location where compression is different.
   */
  def transformAndTransfer(compressionOpt: Option[Compression], file: FileObject, modtagers: List[Modtager]): Boolean = {
    Compression.getFileHandler(compressionOpt) match {
      case Some(fileHandler) => fileHandler.handle(file, modtagers)
      case _ =>
        val fileName: String = Compression.removeCompressionExtension(file.getName.getBaseName, compressionOpt)
        val stream: InputStream = getInputStream(file, compressionOpt)
        writeStreamToModtagers(file, modtagers, fileName, stream)
    }
  }

  def writeStreamToModtagers(file: FileObject, modtagers: List[Modtager], fileName: String, stream: InputStream): Boolean = {
    val candidates: List[(FileObject, Option[Compression])] = getFilesToWrite(modtagers, fileName)
    val filteredCandidates: List[(FileObject, Option[Compression])] = filterExistingOnes(candidates)
    val candidateStreams: List[OutputStream] = filteredCandidates.map(candidate => {
      candidate._1.createFile()
      candidate._2.fold(candidate._1.getContent.getOutputStream)(compression => {
        Compression.getOutputStream(candidate._1, compression)
      })
    })
    Try(writeAndMove(file, stream, filteredCandidates, candidateStreams)) match {
      case scala.util.Failure(errorMessage) =>
        stream.close()
        logger.error(s"event:Failed to process file --file:$file")
        false
      case Success(result) =>
        stream.close()
        result
    }
  }

  /**
   * Write an input stream to output streams and moves the files to actual location
   * once the write finishes
   */
  private def writeAndMove(file: FileObject, stream: InputStream, filteredCandidates: List[(FileObject, Option[Compression])], candidateStreams: List[OutputStream]): Boolean = {
    Try(writeToCandidates(stream, candidateStreams)) match {
      case Success(result) =>
        candidateStreams.foreach(candidateStream => {
          candidateStream.flush()
          candidateStream.close()
        })
        moveBack(filteredCandidates.map(_._1))
        true
      case Failure(errorMessage) =>
        logger.error(s"event:Failed to deliver file--file:${file.getName.toString}" +
          s"--cause:${errorMessage.toString}\n ${errorMessage.getStackTrace.mkString("\n")}")
        false
    }
  }

  def moveBack(candidates: Seq[FileObject]): Unit = {
    candidates.foreach(
      candidate => {
        candidate.moveTo(
          DatosSettings.fileManager.resolveFile(candidate.getParent.getParent, candidate.getName.getBaseName)
        )
      }
    )
  }

  def filterExistingOnes(candidates: List[(FileObject, Option[Compression])]): List[(FileObject, Option[Compression])] = {
    candidates.filter(
      candidate => {
        candidate._1.exists() match {
          case true =>
            logger.error(s"event: File with same name exists, skipping--file:${candidate._1.getName.toString}")
            false
          case false => true
        }
      }
    )
  }

  /**
   * Create input stream of a file based on compression.
   */
  def getInputStream(fileObject: FileObject, compressionOpt: Option[Compression]): InputStream = {
    compressionOpt.fold(
      {
        val inputStream: InputStream = new BufferedInputStream(fileObject.getContent.getInputStream)
        inputStream
      }
    )(compression => {
        Compression.convertAndCreateStream(fileObject, compression)
      })
  }

  /**
   * Create temporary file objects to be written into.
   */
  def getFilesToWrite(modtagers: List[Modtager], fileName: String): List[(FileObject, Option[Compression])] = {
    modtagers.map(
      modtager => {
        val folder: FileObject = DatosSettings.fileManager.resolveFile(modtager.path)
        val temporaryDirectory: FileObject = DatosSettings.fileManager.resolveFile(folder, DatosSettings.tempDirectory)
        createDirectoryIfNotExists(temporaryDirectory)
        (DatosSettings.fileManager.resolveFile(
          temporaryDirectory,
          modtager.transformer(Compression.nameBasedOnCompression(fileName, modtager.compression))
        ), modtager.compression)
      }
    )
  }

  def writeToCandidates(stream: InputStream, candidateStreams: List[OutputStream]): Unit = {
    val collector: Array[Byte] = new Array[Byte](batchSize)
    var readSize: Int = stream.read(collector)
    while (readSize != -1) {
      candidateStreams.foreach(
        {
          candidateStream =>
            candidateStream.write(collector, 0, readSize)
            candidateStream.flush()
            readSize = stream.read(collector)
        }
      )
    }
  }

  private def createDirectoryIfNotExists(fileObject: FileObject): Unit = {
    fileObject.exists() match {
      case true =>
        fileObject.isFile match {
          case true =>
            logger.error(s"event: Found conflicting file : ${fileObject.getName.toString} , removing")
            fileObject.delete()
            fileObject.createFolder()
          case false =>
          //Do Nothing
        }
      case false =>
        fileObject.createFolder()
    }
  }

}

