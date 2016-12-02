package com.distribuit.datos

import akka.actor.Actor
import com.distribuit.datos.helper.DatosHelper
import com.typesafe.scalalogging.Logger
import com.distribuit.datos.models.{ Failure, DatosRequest, Success }
import org.apache.commons.vfs2.FileObject
import org.slf4j.LoggerFactory

import scala.util.Try

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 *         Reads one file and transfers it to multiple output locations
 */
class Datos extends Actor {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  override def receive: Receive = {
    case datosRequest: DatosRequest =>
      datosRequest.modtagers.forall(modtager => modtager.compression == datosRequest.compression) match {
        case true =>
          sendResult(
            datosRequest,
            logAndGetValue(
              Try(DatosHelper.directTransfer(datosRequest.fileObject, datosRequest.modtagers,
                datosRequest.compression)),
              datosRequest.fileObject
            )
          )
        case false =>
          sendResult(
            datosRequest,
            logAndGetValue(
              Try(DatosHelper.transformAndTransfer(datosRequest.compression, datosRequest.fileObject, datosRequest.modtagers)),
              datosRequest.fileObject
            )
          )
      }
    case invalidModel =>
      logger.error(s"Invalid Model $invalidModel")
  }

  private def logAndGetValue(result: Try[Boolean], fileObject: FileObject): Boolean = result match {
    case scala.util.Success(isSuccessful) => isSuccessful
    case scala.util.Failure(errorMessage) =>
      logger.error(s"event:Failed to process file--file:$fileObject--cause:${errorMessage.toString} \n ${errorMessage.getStackTrace.mkString("\n")}")
      false
  }

  private def sendResult(datosRequest: DatosRequest, status: Boolean): Unit = {
    sender ! (status match {
      case true => Success(datosRequest.id)
      case false => Failure(datosRequest.id)
    })
  }
}

