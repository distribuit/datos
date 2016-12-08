package com.distribuit.datos.reporter

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent on 08/12/16.
 */
trait Reporter extends Actor {

  private val logger = Logger(LoggerFactory.getLogger("Reporter"))

  def reportError(event: String, message: String, cause: String): Unit

  def reportEvent(event: String, message: String): Unit

  override def receive: Receive = {
    case Error(event, message, cause) => reportError(event, message, cause)
    case Event(event, message) => reportEvent(event, message)
    case invalid => logger.error(s"event:Invalid message:message:Received Invalid message--actor:${self.path.toStringWithoutAddress}--type:${invalid.getClass}")
  }
}

object Reporter {
  private val logger = Logger(LoggerFactory.getLogger("Reporter"))

  def getReporter(id: String): Reporter = {
    id.contains(',') match {
      case true =>
        new MultiReporter(id.split(',').toSet.map(buildReporter))
      case false =>
        buildReporter(id)
    }

  }

  private def buildReporter(id: String): Reporter = {
    id match {
      case "None" => Identity
      case _ =>
        logger.error(s"event: Invalid configuration--message:Invalid reporter configured, Will not report--reporter:$id")
        Identity
    }
  }
}

object Identity extends Reporter {
  override def reportError(event: String, message: String, cause: String): Unit = {
    //Do Nothing
  }

  override def reportEvent(event: String, message: String): Unit = {
    //Do Nothing
  }
}

class MultiReporter(reporters: Set[Reporter]) extends Reporter {

  override def reportError(event: String, message: String, cause: String): Unit = reporters.foreach(_.reportError(
    event, message, cause
  ))

  override def reportEvent(event: String, message: String): Unit = reporters.foreach(
    _.reportEvent(event, message)
  )
}