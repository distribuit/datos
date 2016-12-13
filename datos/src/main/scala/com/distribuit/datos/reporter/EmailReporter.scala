package com.distribuit.datos.reporter

import com.distribuit.datos.common.DatosSettings
import org.apache.commons.mail.SimpleEmail

import scala.collection.JavaConverters._

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent on 08/12/16.
 */
class EmailReporter extends Reporter {

  private val host: String = DatosSettings.config.getString("reporter.email.host")

  private val senderName: String = DatosSettings.config.getString("reporter.email.sender")

  private val port: Int = DatosSettings.config.getInt("reporter.email.port")

  private val from: String = DatosSettings.config.getString("reporter.email.from")

  private val to: String = DatosSettings.config.getString("reporter.email.to")

  private val enableTLS: Boolean = DatosSettings.config.getBoolean("reporter.email.tls")

  private val password: String = DatosSettings.config.getString("reporter.email.password")

  override def reportError(event: String, message: String, cause: String): Unit = {
    report(
      "Error during Datos Execution",
      s"Event:$event\n" +
        s"Type:Error\n" +
        s"Message:$message\n" +
        s"Cause:$cause"
    )
  }

  override def reportEvent(event: String, message: String): Unit = {
    report(
      "Datos Event",
      s"Event:$event\n" +
        s"Type:Event\n" +
        s"Message:$message\n"
    )
  }

  private def report(subject: String, message: String): Unit = {
    val email: SimpleEmail = new SimpleEmail()
    email.setFrom(from, senderName)
    email.setAuthentication(from, password)
    email.setHostName(host)
    email.setSmtpPort(port)
    email.setTLS(enableTLS)
    email.setTo(to.split(",").toList.asJavaCollection)
    email.setMsg(message)
    email.setMsg(subject)
    email.send()
  }
}
