package com.distribuit.datos

import akka.actor.{ Actor, ActorRef, Props }
import akka.routing.RoundRobinPool
import com.distribuit.datos.common.DatosSettings
import com.distribuit.datos.models.{ Running, ShutDown, ShuttingDown, Status }

import scala.collection.mutable

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 *         Manages all other actors in Datos system and accept and process requests from external user
 */
class DatosGuardian(val groups: Map[String, mutable.Buffer[WorkerSchema]]) extends Actor {
  val datos: ActorRef = context.actorOf(RoundRobinPool(DatosSettings.datosCount).props(Props(new Datos())).withDispatcher("akka.actor.dispatcher.datos"), "Datos")
  val uniqueIdGenerator: ActorRef = context.actorOf(Props(new UniqueIdGenerator), "UniqueIdGenerator")
  var status: Status = Running
  val groupManagers: Map[String, ActorRef] = groups.map(
    group =>
      group._1 -> context.actorOf(Props(new GroupManager(group._2, datos, uniqueIdGenerator)), group._1)
  )

  sys addShutdownHook {
    status = ShuttingDown
  }

  override def receive: Receive = {
    case ShutDown =>
      datos ! ShutDown
      groupManagers.values.foreach(_ ! ShutDown)
  }
}
