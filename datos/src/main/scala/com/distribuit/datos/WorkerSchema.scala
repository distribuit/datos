package com.distribuit.datos

import com.distribuit.datos.common.OnCompletion
import com.distribuit.datos.models.Candidate
import com.distribuit.datos.models.Modtager

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 */
case class WorkerSchema(name: String, candidate: Candidate, matches: List[String], notMatches: List[String], outPut: List[Modtager], onCompletion: OnCompletion)

