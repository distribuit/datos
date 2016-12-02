package com.distribuit.datos.models

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
abstract sealed class RequestStatus

case class Success(transactionId: String) extends RequestStatus

case class Failure(transactionId: String) extends RequestStatus