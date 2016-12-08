package com.distribuit.datos.message

import com.datos.vfs.FileObject
import com.distribuit.datos.compression.Compression.Compression

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
case class DatosRequest(id: String, fileObject: FileObject, compression: Option[Compression], modtagers: List[Modtager], onCompletion: OnCompletion)