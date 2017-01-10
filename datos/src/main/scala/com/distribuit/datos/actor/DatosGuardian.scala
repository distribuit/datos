package com.distribuit.datos.actor

import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy }
import akka.routing.RoundRobinPool
import com.distribuit.datos.common.DatosSettings
import com.distribuit.datos.message._

import scala.collection.mutable

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 *         Manages all other actors in Datos system and accept and process requests from external user
 */
class DatosGuardian(val groups: Map[String, mutable.Buffer[WorkerSchema]]) extends Actor with ActorLogging {
  val datos: ActorRef = context.actorOf(Props(new Datos(self)).withRouter(RoundRobinPool(nrOfInstances = DatosSettings.config.getInt("datos.count"), supervisorStrategy = {
    OneForOneStrategy() {
      case _ ⇒ SupervisorStrategy.restart
    }
  })).withDispatcher("akka.actor.dispatcher.datos"), name = "Datos")
  val uniqueIdGenerator: ActorRef = context.actorOf(Props(new UniqueIdGenerator), "UniqueIdGenerator")
  val groupManagers: Map[String, ActorRef] = groups.map(
    group =>
      group._1 -> context.actorOf(Props(new GroupManager(group._2, datos, uniqueIdGenerator)), group._1)
  )

  var datosPoolSize: Int = 0

  sys addShutdownHook {
    self ! ShutDown
  }

  override def receive: Receive = {
    case ShutDown =>
      datos ! ShutDown
      groupManagers.values.foreach(_ ! ShutDown)
    case Start =>
      datos ! Start
      groupManagers.values.foreach(_ ! Start)
    case IamAlive(actor) =>
      datosPoolSize = datosPoolSize + 1
    case IamDead(actor) =>
      datosPoolSize = datosPoolSize - 1
  }
}
