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

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import java.util.concurrent.Executors
import play.mvc.Http
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.twowaymessage.assets.Fixtures
import uk.gov.hmrc.twowaymessage.connectors.MessageConnector
import uk.gov.hmrc.twowaymessage.model._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

class TwoWayMessageServiceSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with Fixtures with MockitoSugar {

  implicit val mockExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  implicit val mockHeaderCarrier = HeaderCarrier()
  val mockMessageConnector = mock[MessageConnector]
  val messageService = new TwoWayMessageServiceImpl(mockMessageConnector)

  protected def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  val twoWayMessageReplyExample = TwoWayMessage(
    ContactDetails("someEmail@test.com", None),
    "Question",
    "SGVsbG8gV29ybGQ=",
    Option.apply("replyId")
  )

  "TwoWayMessageService.findMessagesListBy" should {

    val fixtureMessages = conversationItems("5c2dec526900006b000d53b1", "5c2dec526900006b000d53b1")
    "return a list of messages if successfully fetched from the message service" in {

      when(
        mockMessageConnector
          .getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(HttpResponse(Http.Status.OK, Json.parse(fixtureMessages), Map("" -> Seq("", "")))))

      val messagesResult = await(messageService.findMessagesBy("1234567890"))
      messagesResult.right.get.head.validFrom.toString should be("2013-12-01")
    }

    val invalidFixtureMessages = "{}"
    "return error if invalid message list json" in {
      when(
        mockMessageConnector
          .getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(HttpResponse(Http.Status.OK, Json.parse(invalidFixtureMessages), Map("" -> Seq("", "")))))
      val messagesResult = await(messageService.findMessagesBy("1234567890"))
      messagesResult.right should not be (None)
    }

    "return error if there is a problem connecting to the message service" in {
      when(
        mockMessageConnector
          .getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Http.Status.INTERNAL_SERVER_ERROR, "")))
      val messageResult = await(messageService.findMessagesBy("1234567890"))
      messageResult.left.get should be("Error retrieving messages")
    }
  }
}