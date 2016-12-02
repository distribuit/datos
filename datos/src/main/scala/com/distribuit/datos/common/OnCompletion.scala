package com.distribuit.datos.common

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
abstract sealed class OnCompletion

case object Delete extends OnCompletion

case class Move(path: String) extends OnCompletion