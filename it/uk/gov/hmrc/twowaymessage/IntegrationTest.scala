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

import com.google.common.io.BaseEncoding
import play.api.libs.json.Json

import java.util.UUID.randomUUID

class IntegrationTest extends IntegrationSpec {

  "Find message by ID" should "retrieve message successfully" in {
    val messageId = createMessage()

    val response = httpClient
      .url(s"http://localhost:$port/messages/$messageId/content")
      .withHttpHeaders(governmentGatewayUserToken)
      .get()
      .futureValue

    response.status shouldBe 200
    response.body should include("My test message")
  }

  private def createMessage(): String = {
    def messageContent: String =
      BaseEncoding.base64().encode(s"My test message - ${randomUUID()}".getBytes())

    val json =
      s"""
         |{
         |    "externalRef": {
         |        "id": "$randomUUID",
         |        "source": "2WSM"
         |     },
         |     "recipient": {
         |         "taxIdentifier": {
         |             "name": "nino",
         |             "value": "AA000108C"
         |         },
         |         "email": "someEmail@test.com"
         |     },
         |     "messageType": "2wsm-customer",
         |     "subject": "Some subject",
         |     "content": "$messageContent",
         |     "alertDetails": {
         |         "templateId": "c85fc714-8373-4c6d-93de-509a537799b1",
         |         "data": {
         |             "someKey": "someValue"
         |         }
         |     }
         |}
         |""".stripMargin

    val messageApiPort = 8910
    val response = httpClient
      .url(s"http://localhost:$messageApiPort/messages")
      .post(Json.parse(json))
      .futureValue

    (Json.parse(response.body) \ "id").as[String]
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
        |  }
           """.stripMargin

    val authApiPort = 8585
    val response = httpClient
      .url(s"http://localhost:$authApiPort/government-gateway/session/login")
      .withHttpHeaders(("Content-Type", "application/json"))
      .post(GOVERNMENT_GATEWAY_AUTH_PAYLOAD)
      .futureValue

    ("Authorization", response.header("Authorization").get)
  }

}
