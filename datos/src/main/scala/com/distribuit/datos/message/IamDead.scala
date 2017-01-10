package com.distribuit.datos.message

import akka.actor.ActorRef

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent on 10/01/17.
 */
case class IamDead(actor: ActorRef)