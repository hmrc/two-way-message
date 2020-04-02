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

package uk.gov.hmrc.twowaymessage.assets

import org.joda.time.LocalDate
import scala.xml.{Unparsed, Utility, Xhtml}
import uk.gov.hmrc.domain.TaxIds._
import uk.gov.hmrc.domain._
import uk.gov.hmrc.twowaymessage.model.{Message, _}

trait Fixtures {

  val testMessage = Message(
    ExternalRef("some-random-id", "2WSM"),
    Recipient(
      new TaxIdentifier with SimpleName {
        override val name: String = "nino"
        override def value: String = "AB123456C"
      },
      "email@test.com"),
    MessageType.Adviser,
    "QUESTION",
    "some base64-encoded-html",
    Details(
      FormId.Reply,
      Some("reply-to-id"),
      Some("thread-id"),
      Some("P800"),
      Some(Adviser(pidId = "adviser-id")))
  )

  val testConversationItem = ConversationItem(
    id = "5d02201b5b0000360151779e",
    subject = "test subject",
    body = Some(
      ConversationItemDetails(
      `type` = MessageType.Adviser,
      form = FormId.Reply,
      issueDate = Some(LocalDate.now),
      replyTo = Some("reply-to-id"),
      enquiryType = Some("test-enquiry-type"),
      adviser = Some(Adviser(pidId = "adviser-id")))),
    validFrom = LocalDate.now,
    content = Some("test-content")
  )

  def conversationItem(id:String): String =
     s"""
     | {
     |     "renderUrl": {
     |         "url": "relUrl",
     |         "service": "service"
     |     },
     |     "statutory": false,
     |     "hash": "24d5d7da-1b11-4d38-a730-b2f952969440",
     |     "lastUpdated": 1554296147553,
     |     "status": "todo",
     |     "readTime": 1554296147548,
     |     "alerts": {
     |         "success": true,
     |         "alertTime": 1554296147552,
     |         "emailAddress": "2afd5ea4-b12e-4851-82d2-774cef6b3c83@test.com"
     |     },
     |     "alertDetails": {
     |         "data": {
     |         },
     |         "templateId": "templateId"
     |     },
     |     "alertFrom": "2013-12-01",
     |     "validFrom": "2013-12-01",
     |     "body": {
     |         "type": "2wsm-advisor",
     |         "adviser": {
     |             "pidId": "adviser-id"
     |         },
     |         "enquiryType": "enquiry-type",
     |         "threadId": "530410d70000000000000000",
     |         "issueDate": "2019-04-03",
     |         "detailsId": "C0123456781234568",
     |         "suppressedAt": "2013-01-02",
     |         "form": "2WSM-question"
     |     },
     |     "subject": "Blah blah blah",
     |     "recipient": {
     |         "identifier": {
     |             "value": "8000045498",
     |             "name": "sautr"
     |         },
     |         "regime": "sa"
     |     },
     |     "id": "${id}"
     | }
     """.stripMargin

  def conversationItems(id1:String, id2: String): String =
    s"""
           | [
           | ${conversationItem(id1)},
           | ${conversationItem(id2)}
           | ]
         """.stripMargin

  def expectedPdfHtml(subject: String): String = {
    val doctype = "<!DOCTYPE html>"
    val css = """body,header {display:block;margin:0;font-family:Arial,sans-serif;font-size:1.5em;color:black;}
                 | .govuk-header-question {font-size:2.25rem;text-align:center;}
                 | .govuk-heading-xl {font-size:2em;}
                 | .govuk-heading-l {font-size:1.5em;}
                 | .govuk-header__logo {width:33.33%;box-sizing:border-box;margin-bottom:10px;float:left;vertical-align:top;padding-left:10px;}
                 | .govuk-header__logotype {-webkit-font-smoothing:antialiased;font-weight:700;font-size:30px;line-height:30px;color:white;}
                 | .govuk-header__logotype-crown {margin-right:1px;position:relative;top:-4px;vertical-align:middle;}
                 | .govuk-header__link:link, .govuk-header__link:visited {text-decoration:none;color:white;}
                 | .govuk-header__link--homepage {-webkit-font-smoothing:antialiased;font-weight:700;font-size:30px;line-height:30px;}
                 | .govuk-width-container {max-width:960px;margin:0 auto;padding-left:10px;padding-right:10px;}
                 | .govuk-header__container {position:relative;margin-bottom:-10px;padding-top:10px;border-bottom:10px solid;}
                 | .govuk-header__content {box-sizing:border-box;float:left;padding-left:15px;padding-right:10px;width:66.66%;}
                 | .govuk-font-weight-bold {font-weight:bold;}
                 | .faded-text--small {font-size:large;font-weight:bold;color:gray;}
                 | #header_top {border-bottom:none;}
                 | #header {background:black;color:white;}
                 | #header_bottom {clear:both;border-bottom-color:black;}
                 | #nino {padding-right:20px;}
                 | #internal {font-family:Arial,sans-serif;font-size:1.5rem;float:right}
                 | #indented {padding-left: 20px;}
                 | #subject {padding-right: 10px;}""".stripMargin.split("\n").mkString

    val html = Xhtml.toXhtml(Utility.trim(
      <html class="govuk-template" lang="en">
        <head>
          <title>GOV.UK - The best place to find government services and information</title>
          <meta charset="UTF-8"/>
          <style>{css}</style>
        </head>
        <body class="govuk-template__body ">
          <header id="header">
            <div class="govuk-header__container govuk-width-container" id="header_top">
              <div class="govuk-header__logo">
                <span class="govuk-header__logotype">
                  <svg width="36" height="32" viewbox="0 0 132 97" class="govuk-header__logotype-crown" role="presentation"  xmlns="http://www.w3.org/2000/svg">
                    <path d="M25 30.2c3.5 1.5 7.7-.2 9.1-3.7 1.5-3.6-.2-7.8-3.9-9.2-3.6-1.4-7.6.3-9.1 3.9-1.4 3.5.3 7.5 3.9 9zM9 39.5c3.6 1.5 7.8-.2 9.2-3.7 1.5-3.6-.2-7.8-3.9-9.1-3.6-1.5-7.6.2-9.1 3.8-1.4 3.5.3 7.5 3.8 9zM4.4 57.2c3.5 1.5 7.7-.2 9.1-3.8 1.5-3.6-.2-7.7-3.9-9.1-3.5-1.5-7.6.3-9.1 3.8-1.4 3.5.3 7.6 3.9 9.1zm38.3-21.4c3.5 1.5 7.7-.2 9.1-3.8 1.5-3.6-.2-7.7-3.9-9.1-3.6-1.5-7.6.3-9.1 3.8-1.3 3.6.4 7.7 3.9 9.1zm64.4-5.6c-3.6 1.5-7.8-.2-9.1-3.7-1.5-3.6.2-7.8 3.8-9.2 3.6-1.4 7.7.3 9.2 3.9 1.3 3.5-.4 7.5-3.9 9zm15.9 9.3c-3.6 1.5-7.7-.2-9.1-3.7-1.5-3.6.2-7.8 3.7-9.1 3.6-1.5 7.7.2 9.2 3.8 1.5 3.5-.3 7.5-3.8 9zm4.7 17.7c-3.6 1.5-7.8-.2-9.2-3.8-1.5-3.6.2-7.7 3.9-9.1 3.6-1.5 7.7.3 9.2 3.8 1.3 3.5-.4 7.6-3.9 9.1zM89.3 35.8c-3.6 1.5-7.8-.2-9.2-3.8-1.4-3.6.2-7.7 3.9-9.1 3.6-1.5 7.7.3 9.2 3.8 1.4 3.6-.3 7.7-3.9 9.1zM69.7 17.7l8.9 4.7V9.3l-8.9 2.8c-.2-.3-.5-.6-.9-.9L72.4 0H59.6l3.5 11.2c-.3.3-.6.5-.9.9l-8.8-2.8v13.1l8.8-4.7c.3.3.6.7.9.9l-5 15.4v.1c-.2.8-.4 1.6-.4 2.4 0 4.1 3.1 7.5 7 8.1h.2c.3 0 .7.1 1 .1.4 0 .7 0 1-.1h.2c4-.6 7.1-4.1 7.1-8.1 0-.8-.1-1.7-.4-2.4V34l-5.1-15.4c.4-.2.7-.6 1-.9zM66 92.8c16.9 0 32.8 1.1 47.1 3.2 4-16.9 8.9-26.7 14-33.5l-9.6-3.4c1 4.9 1.1 7.2 0 10.2-1.5-1.4-3-4.3-4.2-8.7L108.6 76c2.8-2 5-3.2 7.5-3.3-4.4 9.4-10 11.9-13.6 11.2-4.3-.8-6.3-4.6-5.6-7.9 1-4.7 5.7-5.9 8-.5 4.3-8.7-3-11.4-7.6-8.8 7.1-7.2 7.9-13.5 2.1-21.1-8 6.1-8.1 12.3-4.5 20.8-4.7-5.4-12.1-2.5-9.5 6.2 3.4-5.2 7.9-2 7.2 3.1-.6 4.3-6.4 7.8-13.5 7.2-10.3-.9-10.9-8-11.2-13.8 2.5-.5 7.1 1.8 11 7.3L80.2 60c-4.1 4.4-8 5.3-12.3 5.4 1.4-4.4 8-11.6 8-11.6H55.5s6.4 7.2 7.9 11.6c-4.2-.1-8-1-12.3-5.4l1.4 16.4c3.9-5.5 8.5-7.7 10.9-7.3-.3 5.8-.9 12.8-11.1 13.8-7.2.6-12.9-2.9-13.5-7.2-.7-5 3.8-8.3 7.1-3.1 2.7-8.7-4.6-11.6-9.4-6.2 3.7-8.5 3.6-14.7-4.6-20.8-5.8 7.6-5 13.9 2.2 21.1-4.7-2.6-11.9.1-7.7 8.8 2.3-5.5 7.1-4.2 8.1.5.7 3.3-1.3 7.1-5.7 7.9-3.5.7-9-1.8-13.5-11.2 2.5.1 4.7 1.3 7.5 3.3l-4.7-15.4c-1.2 4.4-2.7 7.2-4.3 8.7-1.1-3-.9-5.3 0-10.2l-9.5 3.4c5 6.9 9.9 16.7 14 33.5 14.8-2.1 30.8-3.2 47.7-3.2z" fill-rule="evenodd" fill="currentColor"></path>
                  </svg>
                  <span class="govuk-header__logotype-text">GOV.UK</span>
                </span>
              </div>
              <div class="govuk-header__content">
                <span id="internal">Internal HMRC Copy</span>
              </div>
            </div>
            <div class="govuk-header__container govuk-width-container" id="header_bottom">
              <div class="govuk-header-question">P800 Secure message question</div>
            </div>
          </header>
          <div class="govuk-width-container">
            <main role="main" id="main-content" class="govuk-main-wrapper">
              <h1 class="govuk-heading-xl">Summary</h1>
              <h2 class="govuk-heading-l">Customer details</h2>
              <hr/>
              <p class="govuk-body-l">
                <span class="govuk-font-weight-bold" id="nino">National insurance number</span>
                AB234567C</p>
              <hr/>
              <h2 class="govuk-heading-l">To reply to this message, copy this link:</h2>
              <p class="govuk-body-l">
                <a href="http://localhost:8991/two-way-message-adviser-frontend/message/5d02201b5b0000360151779e/reply">http://localhost:8991/two-way-message-adviser-frontend/message/5d02201b5b0000360151779e/reply</a>
              </p>
              <hr/>
              <h2 class="govuk-heading-l">Conversation details</h2>
              <p class="govuk-body-l">
                <span class="govuk-font-weight-bold" id="subject">Subject:</span>{Unparsed(subject)}</p>
              <hr/>
              <div class="govuk-body-l" id="indented">
                <p class="faded-text--small">13 June 2019 by HMRC:</p> <div>
                <p>Dear TestUser</p>
                <p>Thank you for your message of 13 June 2019.</p>
                <p>To recap your question, I think you're asking for help with</p>
                <p>I believe this answers your question and hope you are satisfied with the response.</p>
                <p> If you think there is something important missing, use the link at the end of this message to find out how to contact HMRC.</p>
                <p>Regards<br/>Matthew Groom<br/>HMRC digital team</p>
              </div> <hr/> <p class="faded-text--small">13 June 2019 by the customer:</p> <div>
                <p>Hello&#160;this&#160;is&#160;a&#160;test!</p>
              </div>
              </div>
              <hr/>
            </main>
          </div>
        </body>
      </html>))
    doctype ++ html
  }

}
