package com.distribuit.datos.actor

import akka.actor._
import akka.event.Logging
import com.distribuit.datos.common.DatosSettings
import com.distribuit.datos.message.{ Refresh, ShutDown, Start, WorkerSchema }

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 *         Manages a group of actors each of these listening to a directory
 */
class GroupManager(workerDefinitions: mutable.Buffer[WorkerSchema], val datos: ActorRef, val uniqueIdGenerator: ActorRef) extends Actor with ActorLogging {
  private val logger = Logging(context.system, this)

  val workers: Map[String, ActorRef] = workerDefinitions.map(definitions => {
    definitions.name -> context.actorOf(Props(new Worker(definitions, datos, uniqueIdGenerator)).withDispatcher("akka.actor.dispatcher.datos"), definitions.name)
  }).toMap
  val schedule: Cancellable = context.system.scheduler.schedule(0 seconds, DatosSettings.config.getInt("datos.batch.refresh.interval.seconds") seconds, self, Refresh)(
    context.system.dispatcher
  )

  override def receive: Receive = {
    case Refresh =>
      workers.values.foreach(_ ! Refresh)
    case Start =>
      workers.values.foreach(_ ! Start)
    case ShutDown =>
      schedule.cancel()
      workers.values.foreach(_ ! ShutDown)
    case invalidModel =>
      logger.error(s"event:Invalid model $invalidModel")
  }

}