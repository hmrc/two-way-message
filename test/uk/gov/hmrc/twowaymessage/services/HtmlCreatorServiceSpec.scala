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

package uk.gov.hmrc.twowaymessage.services

import com.codahale.metrics.SharedMetricRegistries
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ Injector, bind }
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.twowaymessage.assets.Fixtures
import uk.gov.hmrc.twowaymessage.model._

import scala.concurrent.{ ExecutionContext, Future }
import scala.xml.{ Utility, Xhtml }

class HtmlCreatorServiceSpec extends PlaySpec with GuiceOneAppPerSuite with Fixtures with MockitoSugar {

  implicit val mockExecutionContext: ExecutionContext = mock[ExecutionContext]
  implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]

  lazy val mockhttpClient: HttpClient = mock[HttpClient]
  lazy val mockServiceConfig: ServicesConfig = mock[ServicesConfig]

  val mockTwoWayMessageService: TwoWayMessageService = mock[TwoWayMessageService]

  val injector: Injector = new GuiceApplicationBuilder()
    .overrides(bind[TwoWayMessageService].to(mockTwoWayMessageService))
    .injector()

  implicit val htmlCreatorService: HtmlCreatorServiceImpl = injector.instanceOf[HtmlCreatorServiceImpl]

  val latestMessageId = "5d02201b5b0000360151779e"

  val listOfConversationItems = List(
    ConversationItem(
      "5d02201b5b0000360151779e",
      "Matt Test 1",
      Some(
        ConversationItemDetails(
          MessageType.Adviser,
          FormId.Reply,
          Some(LocalDate.parse("2019-06-13")),
          Some("5d021fbe5b0000200151779c"),
          Some("P800"))),
      LocalDate.parse("2019-06-13"),
      Some(
        "<p>Dear TestUser</p><p>Thank you for your message of 13 June 2019.</p><p>To recap your question, " +
          "I think you're asking for help with</p><p>I believe this answers your question and hope you are satisfied with the response.</p>" +
          "<p>If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.</p>" +
          "<p>Regards<br>Matthew Groom<br>HMRC digital team</p>")
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
      Some("<p>Hello&nbsp;this&nbsp;is&nbsp;a&nbsp;test!</p>")
    )
  )

  "createConversation" should {
    "create HTML for a customer" in {
      when(
        mockTwoWayMessageService
          .findMessagesBy(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(listOfConversationItems)))
      val result =
        await(htmlCreatorService.createConversation(latestMessageId, listOfConversationItems, RenderType.CustomerLink))
      result mustBe
        Right(
          Html.apply(
            Xhtml.toXhtml(<h1 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h1>
          <p class="faded-text--small">This message was sent to you on 13 June 2019</p> ++
              Utility.trim(<div>
              <p>Dear TestUser</p>
              <p>Thank you for your message of 13 June 2019.</p>
              <p>To recap your question, I think you're asking for help with</p>
              <p>I believe this answers your question and hope you are satisfied with the response.</p>
              <p>If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.</p>
              <p>Regards<br/>Matthew Groom<br/>HMRC digital team</p>
            </div>) ++
              <a href="https://www.gov.uk/government/organisations/hm-revenue-customs/contact/income-tax-enquiries-for-individuals-pensioners-and-employees" target="_blank" rel="noopener noreferrer">Contact HMRC (opens in a new window or tab)</a>
          <hr/>
          <h2 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h2>
          <p class="faded-text--small">You sent this message on 13 June 2019</p>
          <div><p>Hello&#160;this&#160;is&#160;a&#160;test!</p></div>)))
    }

    "create HTML content for an advisor" in {
      when(
        mockTwoWayMessageService
          .findMessagesBy(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(listOfConversationItems)))
      val result =
        await(htmlCreatorService.createConversation(latestMessageId, listOfConversationItems, RenderType.Adviser))
      result mustBe
        Right(
          Html(
            Xhtml.toXhtml(<p class="faded-text--small">13 June 2019 by HMRC:</p> ++
              Utility.trim(<div>
              <p>Dear TestUser</p>
              <p>Thank you for your message of 13 June 2019.</p>
              <p>To recap your question, I think you're asking for help with</p>
              <p>I believe this answers your question and hope you are satisfied with the response.</p>
              <p>If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.</p>
              <p>Regards<br/>Matthew Groom<br/>HMRC digital team</p>
            </div>) ++
              <hr/>
          <p class="faded-text--small">13 June 2019 by the customer:</p>
          <div><p>Hello&#160;this&#160;is&#160;a&#160;test!</p></div>)))
    }
    SharedMetricRegistries.clear()
  }

  "createSingleMessageHtml" should {

    def conversationItem(subject: String): ConversationItem = ConversationItem(
      "5d021fbe5b0000200151779c",
      subject,
      Some(
        ConversationItemDetails(
          MessageType.Customer,
          FormId.Question,
          Some(LocalDate.parse("2019-06-13")),
          None,
          Some("p800"))),
      LocalDate.parse("2019-06-13"),
      Some("Hello, my friend!")
    )

    "create one HTML message for the first message" in {
      val result = await(htmlCreatorService.createSingleMessageHtml(conversationItem("Matt Test 1")))
      result mustBe
        Right(
          Html.apply(Xhtml.toXhtml(<h1 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h1>
          <p class="faded-text--small">You sent this message on 13 June 2019</p>
          <div>Hello, my friend!</div>)))
    }

    "create one HTML message with escaped HTML subject text for the first message" in {
      val result = await(htmlCreatorService.createSingleMessageHtml(conversationItem("&lt;h1&gt;A &amp; B&lt;/h1&gt;")))
      result mustBe
        Right(
          Html.apply(Xhtml.toXhtml(
            <h1 class="govuk-heading-xl margin-top-small margin-bottom-small">&lt;h1&gt;A &amp; B&lt;/h1&gt;</h1>
          <p class="faded-text--small">You sent this message on 13 June 2019</p>
          <div>Hello, my friend!</div>)))
    }

    "create one HTML message with non-escaped HTML subject for the first message" in {
      val result = await(htmlCreatorService.createSingleMessageHtml(conversationItem("<h1>A & B</h1>")))
      result mustBe
        Right(
          Html(Xhtml.toXhtml(
            <h1 class="govuk-heading-xl margin-top-small margin-bottom-small">&lt;h1&gt;A &amp; B&lt;/h1&gt;</h1>
          <p class="faded-text--small">You sent this message on 13 June 2019</p>
          <div>Hello, my friend!</div>)))
    }

  }

  "createHtmlForPdf" should {

    val subjectWithEscapedChars =
      "&lt;b&gt;This is another test to see if this &gt; that &amp; that &lt; this&lt;/b&gt;"

    "create a complete HTML document" in {
      val subject = "Some subject"
      val result = await(
        htmlCreatorService.createHtmlForPdf(latestMessageId, "AB234567C", listOfConversationItems, "Some subject"))
      result mustBe
        Right(expectedPdfHtml(subject))
    }

    "correctly render escaped HTML in the message subject" in {
      val result = await(
        htmlCreatorService
          .createHtmlForPdf(latestMessageId, "AB234567C", listOfConversationItems, subjectWithEscapedChars))
      result mustBe
        Right(expectedPdfHtml(subjectWithEscapedChars))
    }

    "correctly escape unescaped HTML in the message subject" in {
      val subjectWithUnescapedChars = "<b>This is another test to see if this > that & that < this</b>"
      val result = await(
        htmlCreatorService
          .createHtmlForPdf(latestMessageId, "AB234567C", listOfConversationItems, subjectWithUnescapedChars))
      result mustBe
        Right(expectedPdfHtml(subjectWithEscapedChars))
    }
  }

}
