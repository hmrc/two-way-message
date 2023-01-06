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
import org.joda.time.LocalDate
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.twowaymessage.assets.Fixtures
import uk.gov.hmrc.twowaymessage.model._

import scala.xml.{ Utility, Xhtml }

class HtmlCreatorServiceSpec extends PlaySpec with GuiceOneAppPerSuite with Fixtures with MockitoSugar {

  val injector: Injector = new GuiceApplicationBuilder()
    .injector()

  implicit val htmlCreatorService: HtmlCreatorServiceImpl = injector.instanceOf[HtmlCreatorServiceImpl]

  val latestMessageId = "5d02201b5b0000360151779e"

  private def listOfConversationItems(enquiryType: String) = List(
    ConversationItem(
      "5d02201b5b0000360151779e",
      "Matt Test 1",
      Some(
        ConversationItemDetails(
          MessageType.Adviser,
          FormId.Reply,
          Some(LocalDate.parse("2019-06-13")),
          Some("5d021fbe5b0000200151779c"),
          Some(enquiryType))),
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
          Some(enquiryType))),
      LocalDate.parse("2019-06-13"),
      Some("<p>Hello&nbsp;this&nbsp;is&nbsp;a&nbsp;test!</p>")
    )
  )

  "createConversation" should {
    "create HTML for a customer and p800 enquiryType" in {
      val result = await(
        htmlCreatorService
          .createConversation(latestMessageId, listOfConversationItems("p800"), RenderType.CustomerLink)
      )

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

    "create HTML for a customer and epaye-general enquiryType" in {
      val result = await(
        htmlCreatorService
          .createConversation(latestMessageId, listOfConversationItems("epaye-general"), RenderType.CustomerLink)
      )

      result mustBe
        Right(
          Html.apply(
            Xhtml.toXhtml(
              <h1 class="govuk-heading-xl margin-top-small margin-bottom-small">Matt Test 1</h1>
          <p class="faded-text--small">This message was sent to you on 13 June 2019</p> ++
                Utility.trim(<div>
              <p>Dear TestUser</p>
              <p>Thank you for your message of 13 June 2019.</p>
              <p>To recap your question, I think you're asking for help with</p>
              <p>I believe this answers your question and hope you are satisfied with the response.</p>
              <p>If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.</p>
              <p>Regards<br/>Matthew Groom<br/>HMRC digital team</p>
            </div>) ++

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
          <div><p>Hello&#160;this&#160;is&#160;a&#160;test!</p></div>)))
    }

    "create HTML content for an advisor" in {
      val result = await(
        htmlCreatorService.createConversation(latestMessageId, listOfConversationItems("p800"), RenderType.Adviser)
      )

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
}
