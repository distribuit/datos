package com.distribuit.datos.models

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
abstract sealed class Status

case object Running extends Status

case object ShuttingDown extends Status

case object GetStatus