package com.distribuit.datos.message

import com.distribuit.datos.compression.Compression.Compression

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 */
case class Candidate(path: String, compressionOpt: Option[Compression])