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

import com.codahale.metrics.SharedMetricRegistries
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ Injector, bind }
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.{ FakeHeaders, FakeRequest, Helpers }
import uk.gov.hmrc.auth.core.AuthProvider.{ GovernmentGateway, PrivilegedApplication, Verify }
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{ EmptyPredicate, Predicate }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.twowaymessage.assets.TestUtil
import uk.gov.hmrc.twowaymessage.connectors.mocks.MockAuthConnector
import uk.gov.hmrc.twowaymessage.services.TwoWayMessageService

import scala.concurrent.Future

class TwoWayMessageControllerSpec extends TestUtil with MockAuthConnector {

  val mockMessageService: TwoWayMessageService = mock[TwoWayMessageService]

  override lazy val injector: Injector = new GuiceApplicationBuilder()
    .overrides(bind[TwoWayMessageService].to(mockMessageService))
    .overrides(bind[AuthConnector].to(mockAuthConnector))
    .injector()

  val testTwoWayMessageController: TwoWayMessageController = injector.instanceOf[TwoWayMessageController]

  val authPredicate: Predicate = EmptyPredicate

  val twoWayMessageGood: JsValue = Json.parse("""
                                                |    {
                                                |      "contactDetails": {
                                                |         "email":"someEmail@test.com"
                                                |      },
                                                |      "subject":"QUESTION",
                                                |      "content":"SGVsbG8gV29ybGQ="
                                                |    }""".stripMargin)

  val fakeRequest1: FakeRequest[JsValue] = FakeRequest(
    Helpers.POST,
    routes.TwoWayMessageController.getContentBy("someId").url,
    FakeHeaders(),
    twoWayMessageGood
  )

  "The TwoWayMessageController.getContentBy method" should {

    "return 200 (OK) when the message type is valid" in {
      mockAuthorise(AuthProviders(GovernmentGateway, PrivilegedApplication, Verify))(Future.successful())
      when(mockMessageService.findMessagesBy(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(List())))

      val result = await(testTwoWayMessageController.getContentBy("1", "Customer")(fakeRequest1).run())

      result.header.status mustBe Status.OK
    }

    "return 400 (BAD_REQUEST) when the message type is invalid" in {
      mockAuthorise(AuthProviders(GovernmentGateway, PrivilegedApplication, Verify))(Future.successful())

      val result = await(testTwoWayMessageController.getContentBy(id = "1", msgType = "nfejwk")(fakeRequest1).run())

      result.header.status mustBe Status.BAD_REQUEST
    }

    SharedMetricRegistries.clear()
  }

}
