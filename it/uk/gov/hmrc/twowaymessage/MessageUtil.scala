/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.twowaymessage

import com.google.common.io.BaseEncoding
import play.api.libs.json.JsObject

object MessageUtil {
  import play.api.libs.json.{Json, Reads}

  import scala.util.Random

  implicit val deserialiser: Reads[MessageId] = Json.reads[MessageId]
  def generateContent(): String = {
    val stringLength = 20
    BaseEncoding.base64().encode(s"Hello world! - ${Random.nextString(stringLength)}".getBytes())
  }

  case class MessageId(id: String)

  def buildValidCustomerMessage(): JsObject = {
    val jsonString =
      s"""
         | {
         |   "contactDetails":{
         |      "email": "someEmail@test.com"
         |   },
         |   "subject": "subject",
         |   "content": "$generateContent",
         |   "replyTo": "replyTo"
         | }
      """.stripMargin

    Json.parse(jsonString).as[JsObject]
  }

  def buildInvalidCustomerMessage: JsObject = {
    val jsonString =
      s"""
         | {
         |   "email": "test@test.com",
         |   "content": "$generateContent",
         |   "replyTo": "replyTo"
         | }
    """.stripMargin

    Json.parse(jsonString).as[JsObject]
  }

  def buildValidReplyMessage(): JsObject = {
    val jsonString =
      s"""
         | {
         |   "content": "$generateContent"
         | }
      """.stripMargin
    Json.parse(jsonString).as[JsObject]
  }

  def buildInvalidReplyMessage(): JsObject = {
    val jsonString =
      s"""
         | {
         |   "c": "$generateContent"
         | }
      """.stripMargin
    Json.parse(jsonString).as[JsObject]
  }

}