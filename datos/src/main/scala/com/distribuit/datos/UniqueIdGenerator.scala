package com.distribuit.datos

import java.util.UUID

import akka.actor.Actor
import com.distribuit.datos.models.{ GenerateUniqueId, TransactionId }
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
class UniqueIdGenerator extends Actor {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  override def receive: Receive = {
    case GenerateUniqueId =>
      sender ! TransactionId(UUID.randomUUID().toString)
    case invalidModel =>
      logger.error(s"event:Invalid model $invalidModel")
  }
}
