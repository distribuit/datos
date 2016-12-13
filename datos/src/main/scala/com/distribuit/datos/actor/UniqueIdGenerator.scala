package com.distribuit.datos.actor

import java.util.UUID

import akka.actor.Actor
import akka.event.Logging
import com.distribuit.datos.message.{ GenerateUniqueId, TransactionId }

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
class UniqueIdGenerator extends Actor {
  private val logger = Logging(context.system, this)

  override def receive: Receive = {
    case GenerateUniqueId =>
      sender ! TransactionId(UUID.randomUUID().toString)
    case invalidModel =>
      logger.error(s"event:Invalid model $invalidModel")
  }
}
