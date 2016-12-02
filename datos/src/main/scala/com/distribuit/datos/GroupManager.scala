package com.distribuit.datos

import akka.actor._
import com.distribuit.datos.models.Refresh
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 *         Manages a group of actors each of these listening to a directory
 */
class GroupManager(workerDefinitions: mutable.Buffer[WorkerSchema], val datos: ActorRef, val uniqueIdGenerator: ActorRef) extends Actor {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val workers: Map[String, ActorRef] = workerDefinitions.map(definitions => {
    definitions.name -> context.actorOf(Props(new Worker(definitions, datos, uniqueIdGenerator)).withDispatcher("akka.actor.dispatcher.datos"), definitions.name)
  }).toMap
  context.system.scheduler.schedule(0 seconds, 10 seconds, self, Refresh)(
    context.system.dispatcher
  )

  override def receive: Receive = {
    case Refresh =>
      workers.values.foreach(_ ! Refresh)
    case invalidModel =>
      logger.error(s"event:Invalid model $invalidModel")
  }

}