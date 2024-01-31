/*
 * Copyright 2024 HM Revenue & Customs
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
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ Injector, bind }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthProvider.{ GovernmentGateway, PrivilegedApplication, Verify }
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.twowaymessage.assets.TestUtil
import uk.gov.hmrc.twowaymessage.connectors.mocks.MockAuthConnector
import uk.gov.hmrc.twowaymessage.model.FormId.{ Question, Reply }
import uk.gov.hmrc.twowaymessage.model.MessageType.{ Adviser, Customer }
import uk.gov.hmrc.twowaymessage.model.{ ConversationItem, ConversationItemDetails }
import uk.gov.hmrc.twowaymessage.services.TwoWayMessageService

import scala.concurrent.Future
import scala.xml.{ Utility, Xhtml }

class TwoWayMessageControllerSpec extends TestUtil with MockAuthConnector {

  val mockMessageService: TwoWayMessageService = mock[TwoWayMessageService]

  val injector: Injector = new GuiceApplicationBuilder()
    .overrides(bind[TwoWayMessageService].to(mockMessageService))
    .overrides(bind[AuthConnector].to(mockAuthConnector))
    .injector()

  val testTwoWayMessageController: TwoWayMessageController = injector.instanceOf[TwoWayMessageController]

  "The TwoWayMessageController.getContentBy method" should {

    "return 200 (OK) with the content of the conversation in html for the advisor" in {
      mockAuthorisation()
      when(mockMessageService.findMessagesBy(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(listOfConversationItems())))

      val result = testTwoWayMessageController.getContentBy("56145134123", "Adviser")(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe Xhtml.toXhtml(
        <p class="faded-text--small">13 June 2019 by HMRC:</p> ++
          Utility.trim(
            <div>Dear TestUser
              <br/>
              Thank you for your message of 13 June 2019.
              <br/>
              To recap your question, I think you're asking for help with
              <br/>
              I believe this answers your question and hope you are satisfied with the response.
              <br/>
              If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.
              <br/>
              Regards
              <br/>
              Matthew Groom
              <br/>
              HMRC digital team.
            </div>
          ) ++
          <hr/>
          <p class="faded-text--small">13 June 2019 by the customer:</p>
          <div>Hello, my friend!</div>
      )
    }

    "return 200 (OK) with the content of the conversation in html for the customer" in {
      mockAuthorisation()
      when(mockMessageService.findMessagesBy(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(listOfConversationItems())))

      val result = testTwoWayMessageController.getContentBy("12314124", "Customer")(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe Xhtml.toXhtml(
        <h1 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h1>
          <p class="faded-text--small">This message was sent to you on 13 June 2019</p> ++
          Utility.trim(
            <div>Dear TestUser
              <br/>
              Thank you for your message of 13 June 2019.
              <br/>
              To recap your question, I think you're asking for help with
              <br/>
              I believe this answers your question and hope you are satisfied with the response.
              <br/>
              If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.
              <br/>
              Regards
              <br/>
              Matthew Groom
              <br/>
              HMRC digital team.</div>
          ) ++
          <a href="https://www.gov.uk/government/organisations/hm-revenue-customs/contact/income-tax-enquiries-for-individuals-pensioners-and-employees" target="_blank" rel="noopener noreferrer">Contact HMRC (opens in a new window or tab)</a>
              <hr/>
            <h2 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h2>
            <p class="faded-text--small">You sent this message on 13 June 2019</p>
            <div>Hello, my friend!</div>
      )
    }

    "return 200 (OK) with the content of the conversation in html for the customer and epaye-general enquiry type" in {
      mockAuthorisation()
      when(mockMessageService.findMessagesBy(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(listOfConversationItems(enquiryType = "epaye-general"))))

      val result = testTwoWayMessageController.getContentBy("12314124", "Customer")(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe Xhtml.toXhtml(
        <h1 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h1>
          <p class="faded-text--small">This message was sent to you on 13 June 2019</p> ++
          Utility.trim(<div>Dear TestUser
            <br/>
            Thank you for your message of 13 June 2019.
            <br/>
            To recap your question, I think you're asking for help with
            <br/>
            I believe this answers your question and hope you are satisfied with the response.
            <br/>
            If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.
            <br/>
            Regards
            <br/>
            Matthew Groom
            <br/>
            HMRC digital team.</div>) ++
          Utility.trim(<p>
            <span>
              <a style="text-decoration:none;" href="/two-way-message-frontend/message/customer/epaye-general/5d02201b5b0000360151779e/reply#reply-input-label">
                <svg style="vertical-align:text-top;padding-right:5px;" width="21px" height="20px" viewBox="0 0 33 31" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                  <title>Reply</title>
                  <g id="Page-1" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
                    <g id="icon-reply" fill="#000000" fill-rule="nonzero">
                      <path d="M20.0052977,9.00577935 C27.0039418,9.21272548 32.6139021,14.9512245 32.6139021,22 C32.6139021,25.5463753 31.1938581,28.7610816 28.8913669,31.1065217 C29.2442668,30.1082895 29.4380446,29.1123203 29.4380446,28.1436033 C29.4380446,21.8962314 25.9572992,21.1011463 20.323108,21 L15,21 L15,30 L-1.42108547e-14,15 L15,2.25597319e-13 L15,9 L20,9 L20.0052977,9.00577935 Z" id="Combined-Shape"></path>
                    </g>
                  </g>
                </svg>
              </a>
            </span>
            <a href="/two-way-message-frontend/message/customer/epaye-general/5d02201b5b0000360151779e/reply#reply-input-label">Send another message about this</a>
          </p>) ++
          <hr/>
          <h2 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h2>
          <p class="faded-text--small">You sent this message on 13 June 2019</p>
          <div>Hello, my friend!</div>)
    }

    "return 400 (BAD_REQUEST) when the message type is invalid" in {
      mockAuthorisation()

      val result = testTwoWayMessageController.getContentBy(id = "1", msgType = "nfejwk")(FakeRequest())

      status(result) mustBe BAD_REQUEST
    }

    SharedMetricRegistries.clear()
  }

  private def listOfConversationItems(enquiryType: String = "p800"): List[ConversationItem] = List(
    ConversationItem(
      id = "5d02201b5b0000360151779e",
      subject = "Matt Test 1",
      body = Some(
        ConversationItemDetails(
          `type` = Adviser,
          form = Reply,
          issueDate = Some(LocalDate.parse("2019-06-13")),
          replyTo = Some("5d021fbe5b0000200151779c"),
          enquiryType = Some(enquiryType)
        )
      ),
      validFrom = LocalDate.parse("2019-06-13"),
      content = Some(
        "Dear TestUser<br>Thank you for your message of 13 June 2019.<br>" +
          "To recap your question, I think you're asking for help with<br>I believe this answers your question " +
          "and hope you are satisfied with the response.<br>If you think there is something important missing, " +
          "use the link at the end of this message to find out how to contact HMRC.<br/>Regards<br/>Matthew " +
          "Groom<br/>HMRC digital team."
      )
    ),
    ConversationItem(
      id = "5d021fbe5b0000200151779c",
      subject = "Matt Test 1",
      body = Some(
        ConversationItemDetails(
          `type` = Customer,
          form = Question,
          issueDate = Some(LocalDate.parse("2019-06-13")),
          replyTo = None,
          enquiryType = Some(enquiryType)
        )
      ),
      validFrom = LocalDate.parse("2019-06-13"),
      content = Some("Hello, my friend!")
    )
  )

  private def mockAuthorisation(): Unit =
    mockAuthorise(AuthProviders(GovernmentGateway, PrivilegedApplication, Verify))(Future.successful(()))
}
