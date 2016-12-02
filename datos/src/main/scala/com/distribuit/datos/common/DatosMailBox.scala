package com.distribuit.datos.common

import akka.actor.ActorSystem
import akka.dispatch.{ PriorityGenerator, UnboundedPriorityMailbox }
import com.distribuit.datos.models.ShutDown
import com.typesafe.config.Config

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 */
class DatosMailBox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox({
  PriorityGenerator {
    case ShutDown => 0
    case _ => 1
  }
})
