package com.distribuit.datos.common

import akka.actor.{ ActorRef, ActorSystem }
import com.distribuit.datos.reporter.Reporter

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent on 09/12/16.
 */
object DatosServices {
  val actorSystem: ActorSystem = ActorSystem("DatosSystem", DatosSettings.config)
  val reporter: ActorRef = Reporter.instance
}
