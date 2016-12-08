package com.distribuit.datos.compression

import com.datos.vfs.FileObject
import com.distribuit.datos.message.Modtager

/**
 * Copyright (c) 2016 Distribuit Inc.
 * Handles a specific type of file
 * @author paulson.vincent
 */
abstract class FileHandler {
  def handle(file: FileObject, modtagers: List[Modtager]): Boolean
}
