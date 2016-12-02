package com.distribuit.datos.reporter

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent on 08/12/16.
 */
case class Error(event: String, message: String, cause: String)

case class Event(event: String, message: String)