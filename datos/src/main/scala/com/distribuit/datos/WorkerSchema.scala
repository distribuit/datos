package com.distribuit.datos

import com.distribuit.datos.message.{ Candidate, Modtager, OnCompletion }

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 */
case class WorkerSchema(name: String, candidate: Candidate, matches: List[String], notMatches: List[String], outPut: List[Modtager], onCompletion: OnCompletion)

