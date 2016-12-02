package com.distribuit.datos.models

import com.distribuit.datos.compression.Compression.Compression
import com.distribuit.datos.common.OnCompletion
import org.apache.commons.vfs2.FileObject

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
case class DatosRequest(id: String, fileObject: FileObject, compression: Option[Compression], modtagers: List[Modtager], onCompletion: OnCompletion)