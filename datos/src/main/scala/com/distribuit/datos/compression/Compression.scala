package com.distribuit.datos.compression

import java.io.{ InputStream, OutputStream }
import java.util.zip.{ ZipInputStream, ZipOutputStream }

import com.datos.vfs.FileObject
import org.apache.commons.compress.compressors.bzip2.{ BZip2CompressorInputStream, _ }
import org.apache.commons.compress.compressors.deflate._
import org.apache.commons.compress.compressors.gzip._
import org.apache.commons.compress.compressors.pack200._
import org.apache.commons.compress.compressors.xz._

/**
 * Copyright (c) 2016 Distribuit Inc.
 * @author paulson.vincent
 * Compressions available for read and write
 */
object Compression extends Enumeration {
  type Compression = Value
  val GZIP, BZIP2, ZIP, XZ, DEFLATE, PACK200 = Value
  val identifiers: Map[String, Compression] = Map(
    "GZIP" -> GZIP,
    "BZIP2" -> BZIP2,
    "ZIP" -> ZIP,
    "XZ" -> XZ,
    "DEFLATE" -> DEFLATE,
    "PACK200" -> PACK200
  )

  def convertAndCreateStream(fileObject: FileObject, compression: Compression): InputStream =
    compression match {
      case GZIP =>
        new GzipCompressorInputStream(fileObject.getContent.getInputStream)
      case BZIP2 =>
        new BZip2CompressorInputStream(fileObject.getContent.getInputStream)
      case ZIP =>
        new ZipInputStream(fileObject.getContent.getInputStream)
      case XZ =>
        new XZCompressorInputStream(fileObject.getContent.getInputStream)
      case DEFLATE =>
        new DeflateCompressorInputStream(fileObject.getContent.getInputStream)
      case PACK200 =>
        new Pack200CompressorInputStream(fileObject.getContent.getInputStream)
    }

  def getOutputStream(fileObject: FileObject, compression: Compression): OutputStream =
    compression match {
      case GZIP =>
        new GzipCompressorOutputStream(fileObject.getContent.getOutputStream())
      case BZIP2 =>
        new BZip2CompressorOutputStream(fileObject.getContent.getOutputStream())
      case ZIP =>
        new ZipOutputStream(fileObject.getContent.getOutputStream())
      case XZ =>
        new XZCompressorOutputStream(fileObject.getContent.getOutputStream())
      case DEFLATE =>
        new DeflateCompressorOutputStream(fileObject.getContent.getOutputStream())
      case PACK200 =>
        new Pack200CompressorOutputStream(fileObject.getContent.getOutputStream())
    }

  def nameBasedOnCompression(name: String, compression: Option[Compression]): String = {
    compression.fold(name) {
      case GZIP => appendExtension(".gz", name)
      case BZIP2 => appendExtension(".bz2", name)
      case ZIP => appendExtension(".zip", name)
      case XZ => appendExtension(".xz", name)
      case DEFLATE => name
      case PACK200 => appendExtension(".pack", name)
    }
  }

  def removeCompressionExtension(name: String, compression: Option[Compression]): String = {
    compression.fold(name) {
      case GZIP => removeExtension(".gz", name)
      case BZIP2 => removeExtension(".bz2", name)
      case ZIP => removeExtension(".zip", name)
      case XZ => removeExtension(".xz", name)
      case DEFLATE => name
      case PACK200 => removeExtension(".pack", name)
    }
  }

  private def appendExtension(extension: String, name: String): String = {
    name.endsWith(extension) match {
      case true => name
      case false => name + extension
    }
  }

  private def removeExtension(extension: String, name: String): String = {
    name.endsWith(extension) match {
      case true => name.substring(0, name.length - extension.length)
      case false => name
    }
  }

  def getFileHandler(compression: Option[Compression]): Option[FileHandler] = {
    compression.flatMap({
      case ZIP => Some(ZipFileHandler)
      case _ => None
    })
  }
}
