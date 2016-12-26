package com.distribuit.datos

import akka.actor.{ ActorRef, Props }
import akka.stream.ActorMaterializer
import com.distribuit.datos.actor.{ DatosGuardian, Worker }
import com.distribuit.datos.common._
import com.distribuit.datos.message.WorkerSchema
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import play.api.libs.json.{ JsValue, Json }

import scala.collection.mutable
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
}
