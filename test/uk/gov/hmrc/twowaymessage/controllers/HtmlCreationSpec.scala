/*
 * Copyright 2020 HM Revenue & Customs
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

import java.util.{Base64, UUID}

import com.codahale.metrics.SharedMetricRegistries
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results.Created
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import play.mvc.Http
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.{AuthProviders, _}
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.twowaymessage.assets.TestUtil
import uk.gov.hmrc.twowaymessage.connector.mocks.MockAuthConnector
import uk.gov.hmrc.twowaymessage.connectors.MessageConnector
import uk.gov.hmrc.twowaymessage.model.MessageFormat._
import uk.gov.hmrc.twowaymessage.model._
import uk.gov.hmrc.twowaymessage.services.TwoWayMessageService

import scala.concurrent.Future
import scala.xml.{Utility, Xhtml}

class HtmlCreationSpec extends TestUtil with MockAuthConnector {

  val mockMessageService = mock[TwoWayMessageService]
  val mockMessageConnector = mock[MessageConnector]

  override lazy val injector = new GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].to(mockAuthConnector))
    .overrides(bind[MessageConnector].to(mockMessageConnector))
    .injector()

  val testTwoWayMessageController = injector.instanceOf[TwoWayMessageController]

  val authPredicate: Predicate = EmptyPredicate

  val twoWayMessageGood = Json.parse("""
                                       |    {
                                       |      "contactDetails": {
                                       |         "email":"someEmail@test.com"
                                       |      },
                                       |      "subject":"QUESTION",
                                       |      "content":"SGVsbG8gV29ybGQ="
                                       |    }""".stripMargin)

  val fakeRequest1 = FakeRequest(
    Helpers.POST,
    routes.TwoWayMessageController.createMessage("queueName").url,
    FakeHeaders(),
    twoWayMessageGood)

  val listOfConversationItems = List(
    ConversationItem(
      id = "5d02201b5b0000360151779e",
      "Matt Test 1",
      Some(
        ConversationItemDetails(
          MessageType.Adviser,
          FormId.Reply,
          Some(LocalDate.parse("2019-06-13")),
          Some("5d021fbe5b0000200151779c"),
          Some("P800"))),
      LocalDate.parse("2019-06-13"),
      Some(Base64.getEncoder.encodeToString(
        "Dear TestUser<br>Thank you for your message of 13 June 2019.<br>To recap your question, I think you're asking for help with<br>I believe this answers your question and hope you are satisfied with the response.<br>If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.<br/>Regards<br/>Matthew Groom<br/>HMRC digital team.".getBytes))
    ),
    ConversationItem(
      "5d021fbe5b0000200151779c",
      "Matt Test 1",
      Some(
        ConversationItemDetails(
          MessageType.Customer,
          FormId.Question,
          Some(LocalDate.parse("2019-06-13")),
          None,
          Some("p800"))),
      LocalDate.parse("2019-06-13"),
      Some(Base64.getEncoder.encodeToString("Hello, my friend!".getBytes))
    )
  )

  "The TwoWayMessageController.getContentBy method" should {
    "return 200 (OK) with the content of the conversation in html for the advisor" in {
      val nino = Nino("AB123456C")
      mockAuthorise(Enrolment("HMRC-NI") or AuthProviders(PrivilegedApplication))(Future.successful(Some(nino.value)))
      when(
        mockMessageService.postCustomerReply(any[TwoWayMessageReply], ArgumentMatchers.eq("replyTo"))(
          any[HeaderCarrier]))
        .thenReturn(Future.successful(Created(Json.toJson("id" -> UUID.randomUUID().toString))))

      when(
        mockMessageConnector
          .getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Http.Status.OK, Some(Json.toJson(listOfConversationItems)))))

      val result =
        await(testTwoWayMessageController.getContentBy("5d02201b5b0000360151779e", "Adviser")(fakeRequest1).run())
      status(result) shouldBe Status.OK
      bodyOf(result) shouldBe Xhtml.toXhtml(
        <p class="faded-text--small">13 June 2019 by HMRC:</p> ++
        Utility.trim(<div>Dear TestUser<br/> Thank you for your message of 13 June 2019.<br/>
          To recap your question, I think you're asking for help with<br/>
          I believe this answers your question and hope you are satisfied with the response.<br/>
          If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.<br/>
          Regards<br/>
          Matthew Groom<br/>
          HMRC digital team.</div>) ++
        <hr/>
        <p class="faded-text--small">13 June 2019 by the customer:</p>
        <div>Hello, my friend!</div>)
    }

    "return 200 (OK) with the content of the conversation in html for the customer" in {
      val nino = Nino("AB123456C")
      mockAuthorise(Enrolment("HMRC-NI") or AuthProviders(PrivilegedApplication))(Future.successful(Some(nino.value)))
      when(
        mockMessageService.postCustomerReply(any[TwoWayMessageReply], ArgumentMatchers.eq("replyTo"))(
          any[HeaderCarrier]))
        .thenReturn(Future.successful(Created(Json.toJson("id" -> UUID.randomUUID().toString))))

      when(
        mockMessageConnector
          .getMessages(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Http.Status.OK, Some(Json.toJson(listOfConversationItems)))))

      val result =
        await(testTwoWayMessageController.getContentBy("5d02201b5b0000360151779e", "Customer")(fakeRequest1).run())
      status(result) shouldBe Status.OK
      bodyOf(result) shouldBe Xhtml.toXhtml(
        <h1 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h1>
          <p class="faded-text--small">This message was sent to you on 13 June 2019</p> ++
          Utility.trim(<div>Dear TestUser<br/>Thank you for your message of 13 June 2019.<br/>
            To recap your question, I think you're asking for help with<br/>
            I believe this answers your question and hope you are satisfied with the response.<br/>
            If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.<br/>
            Regards<br/>
            Matthew Groom<br/>
            HMRC digital team.</div>) ++
          <a href="https://www.gov.uk/government/organisations/hm-revenue-customs/contact/income-tax-enquiries-for-individuals-pensioners-and-employees" target="_blank" rel="noopener noreferrer">Contact HMRC (opens in a new window or tab)</a>
          <hr/>
          <h2 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h2>
          <p class="faded-text--small">You sent this message on 13 June 2019</p>
          <div>Hello, my friend!</div>)
    }

    "return 400 (bad request)  with no content in body" in {
      val nino = Nino("AB123456C")
      mockAuthorise(Enrolment("HMRC-NI") or AuthProviders(PrivilegedApplication))(Future.successful(Some(nino.value)))
      when(
        mockMessageService.postCustomerReply(any[TwoWayMessageReply], ArgumentMatchers.eq("replyTo"))(
          any[HeaderCarrier]))
        .thenReturn(Future.successful(Created(Json.toJson("id" -> UUID.randomUUID().toString))))
      val result = await(testTwoWayMessageController.getContentBy("1", "nfejwk")(fakeRequest1).run())
      status(result) shouldBe Status.BAD_REQUEST
    }

    SharedMetricRegistries.clear
  }

}
