package com.distribuit.datos.compression

import java.io.OutputStream
import java.util.zip.{ ZipEntry, ZipInputStream }

import com.datos.vfs.FileObject
import com.distribuit.datos.actor.helper.DatosHelper
import com.distribuit.datos.compression.Compression.Compression
import com.distribuit.datos.message.Modtager
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

/**
 * Handle movement of zip files
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
object ZipFileHandler extends FileHandler {

  private val logger = Logger(LoggerFactory.getLogger("ZipFileHandler"))

  def handle(file: FileObject, modtagers: List[Modtager]): Boolean = {
    val stream: ZipInputStream = new ZipInputStream(file.getContent.getInputStream)
    var entry: ZipEntry = stream.getNextEntry
    val listOfFilesWritten: mutable.ListBuffer[FileObject] = new mutable.ListBuffer[FileObject]
    var isSuccessFul: Boolean = true
    while (entry != null) {
      val fileName: String = entry.getName
      val candidates: List[(FileObject, Option[Compression])] = DatosHelper.getFilesToWrite(modtagers, fileName)
      val filteredCandidates: List[(FileObject, Option[Compression])] = DatosHelper.filterExistingOnes(candidates)
      val candidateStreams: List[OutputStream] = filteredCandidates.map(candidate => {
        candidate._1.createFile()
        candidate._1.getContent.getOutputStream
      })
      Try(DatosHelper.writeToCandidates(stream, candidateStreams)) match {
        case Success(result) =>
          candidateStreams.foreach(candidateStream => {
            candidateStream.flush()
            candidateStream.close()
          })
          listOfFilesWritten ++= filteredCandidates.map(_._1)
          stream.closeEntry()
          entry = stream.getNextEntry
        case Failure(errorMessage) =>
          isSuccessFul = false
          entry = null
          logger.error(s"event:Failed to deliver file--file:$fileName--zip:$file" +
            s"--cause:${errorMessage.toString}\n ${errorMessage.getStackTrace.mkString("\n")}")
      }
    }
    stream.close()
    isSuccessFul match {
      case true =>
        Try(DatosHelper.moveBack(listOfFilesWritten)) match {
          case Success(result) =>
            true
          case Failure(errorMessage) =>
            logger.error(s"event:Failed to move files of zip file--file:$file")
            false
        }
      case false =>
        false
    }
  }

}
