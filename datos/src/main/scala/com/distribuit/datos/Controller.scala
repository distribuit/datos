package com.distribuit.datos

import akka.actor.{ ActorRef, Props, Terminated }
import akka.stream.ActorMaterializer
import com.distribuit.datos.actor.{ DatosGuardian, Worker }
import com.distribuit.datos.common._
import com.distribuit.datos.message.{ ShutDown, Start, WorkerSchema }
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import play.api.libs.json.{ JsValue, Json }

import scala.collection.mutable
import scala.concurrent.Future
import scala.io.Source
import scala.util.{ Failure, Success, Try }

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 */
object Controller extends App {
  implicit val actorSystem = DatosServices.actorSystem
  implicit val materializer = ActorMaterializer()
  implicit val executor = DatosServices.actorSystem.dispatcher
  private val logger = Logger(LoggerFactory.getLogger("Controller"))
  var started = false
  //  val bindingFuture = Http().bindAndHandle(null, DatosSettings.config.getString("http.interface"), DatosSettings.config.getInt("http.port"))

  val guardian: Option[ActorRef] = Try(extractGroupDefinition) match {
    case Success(groupDefinitionOpt) =>
      groupDefinitionOpt.values.flatten.exists(_.isEmpty) match {
        case true =>
          logger.error("event:schema error--message:Failed to parse default worker schema, exiting")
          print("Exit")
          sys.exit(0)
          None
        case false =>
          val groupDefinition: Map[String, mutable.Buffer[WorkerSchema]] = groupDefinitionOpt.mapValues(options => options.map(_.get))
          Some(DatosServices.actorSystem.actorOf(Props(new DatosGuardian(groupDefinition)), "DatosGuardian"))
      }
    case Failure(errorMessage) =>
      logger.error("event:schema error--msg: Failed to parse Worker.json", errorMessage)
      sys.exit(0)
      None
  }

  def extractGroupDefinition: Map[String, mutable.Buffer[Option[WorkerSchema]]] = {
    val schemaString: String = Source.fromFile("settings/Workers.json").mkString
    val schema: JsValue = Json.parse(schemaString)
    val groups: Map[String, List[JsValue]] = schema.as[Map[String, List[JsValue]]]
    groups.mapValues(workers => {
      val toBuffer: mutable.Buffer[Option[WorkerSchema]] = workers.map(schemaJson =>
        Try(Worker.createWorkerSchema(schemaJson)) match {
          case success: Success[WorkerSchema] => Some(success.value)
          case Failure(errorMessage) =>
            println(s"Cannot parse $schemaJson")
            logger.error(s"event:schema error encounter--message:Failed to parse group definition $schemaJson", errorMessage)
            None
        }).toBuffer
      toBuffer
    })

  }

  start()

  def start() = started match {
    case true =>
      println("Datos is already running")
    case false =>
      guardian match {
        case Some(guardianActor) =>
          guardianActor ! Start
          val startMessage = s"Started ${DatosSettings.config.getInt("datos.count")} instances of datos which will refresh in every ${DatosSettings.config.getInt("datos.batch.refresh.interval.seconds")} seconds, and moves ${DatosSettings.config.getInt("datos.batch.size.mb")} MB blocks"
          println(startMessage)
          logger.info(startMessage)
          started = true
        case None =>
          logger.error("Could not initialize Datos guardian actor")
          throw new Error("Could not initialize Datos guardian actor")
      }
  }

  def stop() = started match {
    case true =>
      guardian.get ! ShutDown
      Thread.sleep(2000)
      // Cool off time
      val terminate: Future[Terminated] = actorSystem.terminate()
      started = false
      println("Stopping datos")
    case false =>
      println("Datos is already stopped")
  }

  sys.addShutdownHook({
    stop()
  })
}
