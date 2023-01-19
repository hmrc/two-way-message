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

import org.apache.commons.net.util.Base64.encodeBase64String
import org.bson.types.ObjectId
import org.mongodb.scala.Document

import java.security.MessageDigest.getInstance
import java.util.Date
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class IntegrationTest extends IntegrationSpec {

  "Find message by ID" should "retrieve message successfully" in {
    val messageId = new ObjectId()
    createTwoWayMessage(messageId)

    val response = httpClient
      .url(s"http://localhost:$port/messages/$messageId/content")
      .withHttpHeaders(governmentGatewayUserToken)
      .get()
      .futureValue

    response.status shouldBe 200
    response.body should include("My test message")
  }

  private def createTwoWayMessage(id: ObjectId): Unit = {
    val hash = encodeBase64String(getInstance("SHA-256").digest(randomUUID().toString.getBytes))
    val document = Document(
      "_id"       -> id,
      "subject"   -> "QUESTION 1",
      "alertFrom" -> "2023-01-18",
      "readTime"  -> new Date(),
      "validFrom" -> "2023-01-18",
      "body" -> Document(
        "form"        -> "2WSM-question",
        "type"        -> "2wsm-customer",
        "paperSent"   -> false,
        "issueDate"   -> "2023-01-18",
        "threadId"    -> "63c8226015c2465b402ef2ad",
        "enquiryType" -> "p800",
        "waitTime"    -> "5 days"
      ),
      "externalRef" -> Document(
        "id"     -> s"$randomUUID",
        "source" -> "2WSM"
      ),
      "content" -> "<h1>My test message</h1>",
      "recipient" -> Document(
        "regime" -> "paye",
        "identifier" -> Document(
          "name"  -> "nino",
          "value" -> "AA000108C"
        ),
        "email" -> "test@test.com"
      ),
      "statutory" -> false,
      "alertDetails" -> Document(
        "templateId" -> "newMessageAlert_2WSM-question",
        "recipientName" -> Document(
          "forename" -> "TestUser",
          "line1"    -> "TestUser"
        ),
        "data" -> Document(
          "email"    -> "test@test.com",
          "waitTime" -> "5 days",
          "date"     -> "2023-01-18",
          "subject"  -> "QUESTION 1"
        )
      ),
      "hash"   -> hash,
      "status" -> "succeeded",
      "renderUrl" -> Document(
        "service" -> "two-way-message",
        "url"     -> s"/messages/$id/content"
      ),
      "lifecycle" -> Document(
        "startedAt" -> new Date(),
        "status" -> Document(
          "name"    -> "SUBMITTED",
          "updated" -> new Date()
        )
      ),
      "lastUpdated" -> new Date(),
      "alerts" -> Document(
        "emailAddress" -> "test@test.com",
        "alertTime"    -> new Date(),
        "success"      -> true
      )
    )

    Await.result(messageCollection.insertOne(document).toFuture, Duration(10, TimeUnit.SECONDS))
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

}
