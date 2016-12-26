package com.distribuit.datos.reporter

import akka.actor.{ Actor, ActorRef, Props }
import akka.event.Logging
import com.distribuit.datos.common.{ DatosServices, DatosSettings }
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent on 08/12/16.
 */
trait Reporter extends Actor {

  private val logger = Logging(context.system, this)

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

  private val IdentityReporter: String = "None"

  private def getReporter(id: String): ActorRef = {
    id.contains(',') match {
      case true =>
        DatosServices.actorSystem.actorOf(Props(new MultiReporter(id.split(',').toSet.map(buildReporter))), "MultiReporter")
      case false =>
        buildReporter(id)
    }
  }

  private def buildReporter(id: String): ActorRef = {
    id match {
      case IdentityReporter => DatosServices.actorSystem.actorOf(Props(new Identity()), "Identity")
      case "slack" => DatosServices.actorSystem.actorOf(Props(new SlackReporter()), "SlackReporter")
      case "email" => DatosServices.actorSystem.actorOf(Props(new EmailReporter()), "EmailReporter")
      case _ =>
        logger.error(s"event: Invalid configuration--message:Invalid reporter configured, Will not report--reporter:$id")
        DatosServices.actorSystem.actorOf(Props(new Identity()), "Identity")
    }
  }

  val instance: ActorRef = {
    DatosSettings.config.getBoolean("reporter.activate") match {
      case true =>
        getReporter(DatosSettings.config.getString("reporter.ids"))
      case false =>
        logger.info("event:No Reporter is configured")
        getReporter(IdentityReporter)
    }
  }

}

class Identity extends Reporter {
  override def reportError(event: String, message: String, cause: String): Unit = {
    //Do Nothing
  }

  override def reportEvent(event: String, message: String): Unit = {
    //Do Nothing
  }
}

class MultiReporter(reporters: Set[ActorRef]) extends Reporter {

  override def reportError(event: String, message: String, cause: String): Unit = reporters.foreach(_ ! Error(
    event, message, cause
  ))

  override def reportEvent(event: String, message: String): Unit = reporters.foreach(
    _ ! (event, message)
  )
}