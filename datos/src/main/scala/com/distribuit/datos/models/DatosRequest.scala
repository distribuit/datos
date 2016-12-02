package com.distribuit.datos.models

import com.datos.vfs.FileObject
import com.distribuit.datos.common.OnCompletion
import com.distribuit.datos.compression.Compression.Compression

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
case class DatosRequest(id: String, fileObject: FileObject, compression: Option[Compression], modtagers: List[Modtager], onCompletion: OnCompletion)