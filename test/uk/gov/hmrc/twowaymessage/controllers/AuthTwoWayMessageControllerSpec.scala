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

package uk.gov.hmrc.twowaymessage.controllers

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthProvider.{GovernmentGateway, PrivilegedApplication, Verify}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.twowaymessage.assets.TestUtil
import uk.gov.hmrc.twowaymessage.connector.mocks.MockAuthConnector
import uk.gov.hmrc.twowaymessage.services.{HtmlCreatorService, TwoWayMessageService}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

class AuthTwoWayMessageControllerSpec extends TestUtil with MockAuthConnector {

  val mockMessageService: TwoWayMessageService = mock[TwoWayMessageService]
  var mockHtmlCreatorService: HtmlCreatorService = mock[HtmlCreatorService]

  val testTwoWayMessageController: TwoWayMessageController = new TwoWayMessageController()

  val fakeRequest1 = FakeRequest("GET", "/1/content")
//  implicit val headerCarrier = HeaderCarrier()

  "The TwoWayMessageController.getContentBy method" should {
    "return 200 (OK) when the message type is valid" in {

      mockAuthorise(AuthProviders(GovernmentGateway, PrivilegedApplication, Verify))(Future.successful())

      when(mockMessageService.findMessagesBy(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(List())))

      val result: Future[Result] = testTwoWayMessageController.getContentBy("1", "Customer")(fakeRequest1)
      status(result) mustBe OK
    }

//    "return 400 (BAD_REQUEST) when the message type is invalid" in {
//      mockAuthorise(AuthProviders(GovernmentGateway, PrivilegedApplication, Verify))(Future.successful())
//      val result = await(testTwoWayMessageController.getContentBy("1", "nfejwk")(fakeRequest1).run())
//      result.header.status mustBe Status.BAD_REQUEST
//    }

//    SharedMetricRegistries.clear
  }
}
