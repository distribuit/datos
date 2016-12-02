package com.distribuit.datos.compression

import com.distribuit.datos.models.Modtager
import org.apache.commons.vfs2.FileObject

/**
 * Copyright (c) 2016 Distribuit Inc.
 * Handles a specific type of file
 * @author paulson.vincent
 */
abstract class FileHandler {
  def handle(file: FileObject, modtagers: List[Modtager]): Boolean
}
