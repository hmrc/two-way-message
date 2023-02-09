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

package uk.gov.hmrc.twowaymessage.services

import com.codahale.metrics.SharedMetricRegistries
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ Injector, bind }
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }
import uk.gov.hmrc.twowaymessage.assets.Fixtures
import uk.gov.hmrc.twowaymessage.connectors.MessageConnector

import scala.concurrent.Future

class TwoWayMessageServiceSpec
    extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Fixtures with MockitoSugar with EitherValues {

  implicit private val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]
  private val mockMessageConnector: MessageConnector = mock[MessageConnector]

  private val injector: Injector = new GuiceApplicationBuilder()
    .overrides(bind[MessageConnector].to(mockMessageConnector))
    .injector()

  private val messageService = injector.instanceOf[TwoWayMessageService]

  "TwoWayMessageService.findMessagesBy" should {

    "return a list of messages if successfully fetched from the message service" in {
      val fixtureMessages = conversationItems("5c2dec526900006b000d53b1", "5c2dec526900006b000d53b1")
      when(mockMessageConnector.getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(HttpResponse(Http.Status.OK, Json.parse(fixtureMessages), Map("" -> Seq("", "")))))

      val messagesResult = await(messageService.findMessagesBy("1234567890"))

      messagesResult.value.head.validFrom.toString should be("2013-12-01")
    }

    "return error if invalid message list json" in {
      val invalidFixtureMessages = "{}"
      when(mockMessageConnector.getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(HttpResponse(Http.Status.OK, Json.parse(invalidFixtureMessages), Map("" -> Seq("", "")))))

      val messagesResult = await(messageService.findMessagesBy("1234567890"))

      messagesResult.right should not be None
    }

    "return error if there is a problem connecting to the message service" in {
      when(mockMessageConnector.getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Http.Status.INTERNAL_SERVER_ERROR, "")))

      val messageResult = await(messageService.findMessagesBy("1234567890"))

      messageResult.left.value should be("Error retrieving messages")
    }

    SharedMetricRegistries.clear()
  }
}
