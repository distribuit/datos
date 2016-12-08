package com.distribuit.datos.reporter

import com.distribuit.datos.common.DatosSettings
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{ CloseableHttpClient, HttpClientBuilder }

/**
 * Copyright (c) 2016 Distribuit Inc.
 *
 * @author paulson.vincent on 08/12/16.
 */
class SlackReporter extends Reporter {

  private val user: String = DatosSettings.config.getString("reporter.slack.user")
  private val channel: String = DatosSettings.config.getString("reporter.slack.channel")
  private val hook: String = DatosSettings.config.getString("reporter.slack.hook")

  private val errorEmoji: String = ":face_with_head_bandage:"

  private val eventFaceEmoji: String = ":neutral_face:"

  override def reportError(event: String, message: String, cause: String): Unit = {
    report(
      s"```Event:$event\n" +
        s"Type:Error\n" +
        s"Message:$message\n" +
        s"Cause:$cause",
      errorEmoji
    )
  }

  override def reportEvent(event: String, message: String): Unit = {
    report(
      s"```Event:$event\n" +
        s"Type:Event\n" +
        s"Message:$message\n",
      errorEmoji
    )
  }

  private def report(message: String, emoji: String): Unit = {
    val params: StringEntity = new StringEntity(
      s"""
         |{
         |"channel": "$channel",
         |"username": "$user",
         |"text": "$message",
         |"icon_emoji": "$emoji"
         |}
       """.stripMargin
    )
    val httpClient: CloseableHttpClient = HttpClientBuilder.create().build()
    val request: HttpPost = new HttpPost(hook)
    request.addHeader("content-type", "application/json")
    request.setEntity(params)
    httpClient.execute(request)
    httpClient.close()
  }

}
