package com.distribuit.datos.message

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent
 */
case class WorkerSchema(name: String, candidate: Candidate, matches: List[String], notMatches: List[String], outPut: List[Modtager], onCompletion: OnCompletion)

