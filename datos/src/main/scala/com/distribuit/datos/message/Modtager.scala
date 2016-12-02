package com.distribuit.datos.message

import com.distribuit.datos.compression.Compression.Compression
import com.distribuit.datos.models.FileNameTransformer

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 */
case class Modtager(path: String, compression: Option[Compression], transformer: FileNameTransformer)