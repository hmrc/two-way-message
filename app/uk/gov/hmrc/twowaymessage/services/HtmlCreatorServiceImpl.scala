/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.Inject
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.twowaymessage.model.{ConversationItem, MessageType}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.XML

class HtmlCreatorServiceImpl @Inject()()
                                      (implicit ec: ExecutionContext) extends HtmlCreatorService {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def createConversation(latestMessageId: String, messages: List[ConversationItem], replyType: RenderType.ReplyType)
                                 (implicit ec: ExecutionContext):Future[Either[String,Html]] = {

    val conversation = createConversationList(messages.sortWith(_.id > _.id), replyType)
    val fullConversation = conversation.mkString("<hr/>")

    Future.successful(Right(Html.apply(fullConversation)))
  }

  override def createSingleMessageHtml(conversationItem: ConversationItem)(implicit ec: ExecutionContext): Future[Either[String,Html]] = {
    Future.successful(Right(Html.apply(format2wsMessageForCustomer(conversationItem,true, false))))
  }

  private def createConversationList(messages: List[ConversationItem], replyType: RenderType.ReplyType ):List[String] = {
    replyType match {
      case RenderType.CustomerLink => messages.sortWith(_.id > _.id)
          .headOption.map {
              hm => format2wsMessageForCustomer(
                hm, isLatestMessage = true, hasSmallSubject = false) :: messages.tail.map(
                  m => format2wsMessageForCustomer(m, isLatestMessage = false))}
          .getOrElse(List.empty)
      case RenderType.CustomerForm => messages.sortWith(_.id > _.id)
          .headOption.map {
              hm => format2wsMessageForCustomer(
                hm, isLatestMessage = true, hasSmallSubject = true) :: messages.tail.map(
                  m => format2wsMessageForCustomer(m, isLatestMessage = false))}
          .getOrElse(List.empty)
      case RenderType.Adviser => messages.sortWith(_.id > _.id).map(format2wsMessageForAdviser(_))
    }
  }

    private def format2wsMessageForAdviser(conversationItem: ConversationItem): String = {
        val message =
        <p class="message_time faded-text--small">
          {getAdviserDatesText(conversationItem)}
        </p>
        <div>{val content = conversationItem.content.getOrElse("")
        XML.loadString("<root>" + content.replaceAllLiterally("<br>","<br/>") + "</root>").child}</div>
      message.mkString
    }

    private def format2wsMessageForCustomer(conversationItem: ConversationItem, isLatestMessage: Boolean,
                                            hasLink:Boolean = true, hasSmallSubject:Boolean = false): String = {
      val headingClass = "govuk-heading-xl margin-top-small margin-bottom-small"
      val header = if (isLatestMessage && !hasSmallSubject) {
        <h1 class={headingClass}>
          {XML.loadString("<root>" + conversationItem.subject + "</root>").child}
        </h1>
      } else {
        <h2 class={headingClass}>
          {XML.loadString("<root>" + conversationItem.subject + "</root>").child}
        </h2>
      }
      val replyForm = if (isLatestMessage && hasLink) {
        val enquiryType = conversationItem.body.flatMap {
          _.enquiryType
        }.getOrElse("")
        val formActionUrl = s"/two-way-message-frontend/message/customer/$enquiryType/" + conversationItem.id + "/reply"
        conversationItem.body.map(_.`type`) match {
          case Some(msgType) => msgType match {
            case MessageType.Adviser =>
              val replyIconBase64 = "iVBORw0KGgoAAAANSUhEUgAAACYAAAAcCAYAAAAN3M1lAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAuIwAALiMBeKU/dgAAAVlpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IlhNUCBDb3JlIDUuNC4wIj4KICAgPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICAgICAgPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIKICAgICAgICAgICAgeG1sbnM6dGlmZj0iaHR0cDovL25zLmFkb2JlLmNvbS90aWZmLzEuMC8iPgogICAgICAgICA8dGlmZjpPcmllbnRhdGlvbj4xPC90aWZmOk9yaWVudGF0aW9uPgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KTMInWQAAApxJREFUWAnNl0trFEEURifxrRsRRBQjiltd6NYHbhQ3Ln0txLXgT1AE/4GK4MaNoAj+AsGNCkJAXApuojHBByKaheA7npPMN/Qk6c5kZjLthWPdqrp16+u61T2x0ai2lc3pdbSXYX11+PLPDrFFRG3HH4UpGAbN+YGbm0fAEfxJmIbXsAK0gQvLxm5+EX6BomQCMj9QYSmdp3WrKUZBEaewNeC8sd3iw6UiuOU29z49IVRBfwrYH4d+muJSgXl5c0q/mTkI92AE7LtI0YrSfDOPgXMmzThux2a+nzAGk81VybVgvgsEpWRpDQx/C37Gemnfk+8BHIaYAtvsDr1s4mnEX6h1vle8IsXcN+ivBq2ttJcYyCmlLS7st68wyynJ/Qg/H++Zk7Pm2iG4D9vAE8n9wm2ZSb63et073tWYIr0mq+AhHAdtSGEOelJbQXHW3WAtF9O4j3ACFJ5x3CWZeXbAKTgN9lNaX8SrcAVaJVVc7CZOjlgRirQ/Dv20oyR7B+bOFbIie6HNWioZ9S2NoB/4Lp4AL6lPaWy35MNMisYe+Azmzz638eeZJRLNe/cBXCRvIeIV16vlTbSk5k9JP+FvKUueD+8uAp6BC99AP4UVH+5xc4+8qefol1rEef/ugn/29FOYGyffeXwfPuW87mSVZaExZ6H4qlet63Qu12Y3C76B4uQpLGrFI180eIkByb2Jda8gwsaiuCqfwSboJLYqT9XcVyb9Tsamc48yUNbmScrmex1fS4INhSTDy3kKhX1K3ZRyIxEjhaip/0WYH9rN4C+N9rJuYbMyGo0zTcePrPZ8tqnn3xzKAbb3NzL32I/s/kzWIS33ayeb+5+c2CjOi3TqanMw/jn1BTy1k3WJmbtvxO1j4hrM/Nr8AwYd2Pnx6sUfAAAAAElFTkSuQmCC"
              <a style="text-decoration:none;padding-right:0.5ex" href={s"$formActionUrl#reply-input-label"}>
                <img style="vertical-align:baseline;" src={"data:image/png;base64," + replyIconBase64} alt="Send another message about this" width="24" height="24"/>
              </a><a href={s"$formActionUrl#reply-input-label"}>Send another message about this</a>.mkString
            case _ => ""
          }
        }
      } else {
        ""
      }
      val xml = header ++ <p class="message_time faded-text--small">
        {getCustomerDateText(conversationItem)}
      </p>
        <div>
          {
          val content = conversationItem.content.getOrElse("")
          XML.loadString("<root>" + content.replaceAllLiterally("<br>","<br/>") + "</root>").child
          }
        </div> ++ replyForm


      xml.mkString

    }

    private def getCustomerDateText(message: ConversationItem): String = {
      val messageDate = extractMessageDate(message)
      message.body match {
        case Some(conversationItemDetails) => conversationItemDetails.`type` match {
          case MessageType.Customer => s"You sent this message on $messageDate"
          case MessageType.Adviser => s"This message was sent to you on $messageDate"
          case _ => defaultDateText(messageDate)
        }
        case _ => defaultDateText(messageDate)
      }
    }

  def getAdviserDatesText(message: ConversationItem): String = {
    val messageDate = extractMessageDate(message)
    message.body match {
      case Some(conversationItemDetails) => conversationItemDetails.`type` match {
        case MessageType.Adviser => s"$messageDate by HMRC"
        case MessageType.Customer=> s"$messageDate by the customer"
        case _ => defaultDateText(messageDate)
      }
      case _ => defaultDateText(messageDate)
    }
  }

    private def defaultDateText(dateStr: String) = s"This message was sent on $dateStr"


    private def extractMessageDate(message: ConversationItem): String = {
      message.body.flatMap(_.issueDate) match {
        case Some(issueDate) => formatter(issueDate)
        case None => formatter(message.validFrom)
      }
    }

    val dateFormatter = DateTimeFormat.forPattern("dd MMMM yyyy")

    private def formatter(date: LocalDate): String = date.toString(dateFormatter)

}
