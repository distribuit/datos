package com.distribuit.datos.common

/**
 * Defines rules to transform a file name
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
trait FileNameTransformer {
  def apply(fileName: String): String
}

object Identity extends FileNameTransformer {
  override def apply(fileName: String): String = fileName
}