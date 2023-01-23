/*
 * Copyright 2023 HM Revenue & Customs
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

import org.bson.types.ObjectId
import org.mongodb.scala.Document
import org.scalatest.BeforeAndAfterEach

import scala.io.Source

class GetMessageContentByIdSpec extends IntegrationSpec with BeforeAndAfterEach {

  behavior of "Get message content by ID"

  it should "successfully render a single customer query message" in {
    val messageId = new ObjectId("603ccbdd190000949264105d")
    createCustomerQueryMessage()

    val response = httpClient
      .url(s"http://localhost:$port/messages/$messageId/content")
      .withHttpHeaders(governmentGatewayUserToken)
      .get()
      .futureValue

    response.status shouldBe 200
    response.body should include("Please answer my test question")
  }

  it should "successfully render a two way message with customer query and adviser reply" in {
    val messageId = new ObjectId("603cccce160000516d600552")
    createCustomerQueryMessage()
    createAdviserReplyMessage()

    val response = httpClient
      .url(s"http://localhost:$port/messages/$messageId/content")
      .withHttpHeaders(governmentGatewayUserToken)
      .get()
      .futureValue

    response.status shouldBe 200
    response.body should include("Please answer my test question")
    response.body should include("This is the answer to your test question")
  }

  private def createCustomerQueryMessage(): Unit = createMessage(source = "customer-query-message.json")

  private def createAdviserReplyMessage(): Unit = createMessage(source = "adviser-reply-message.json")

  private def createMessage(source: String): Unit = {
    val jsonMessage = Source.fromResource(source).mkString
    messageCollection.insertOne(Document(jsonMessage)).toFuture.futureValue
  }

  private def governmentGatewayUserToken: (String, String) = {
    val GOVERNMENT_GATEWAY_AUTH_PAYLOAD =
      """
        | {
        |  "credId": "1234",
        |  "affinityGroup": "Organisation",
        |  "confidenceLevel": 200,
        |  "credentialStrength": "none",
        |  "nino": "AA000108C",
        |  "enrolments": []
        |  }""".stripMargin

    val authApiPort = 8585
    val response = httpClient
      .url(s"http://localhost:$authApiPort/government-gateway/session/login")
      .withHttpHeaders(("Content-Type", "application/json"))
      .post(GOVERNMENT_GATEWAY_AUTH_PAYLOAD)
      .futureValue

    ("Authorization", response.header("Authorization").get)
  }

  override def beforeEach(): Unit =
    messageCollection.deleteMany(Document()).toFuture.futureValue

}