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

package uk.gov.hmrc.twowaymessage.model

import java.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.{ Json, Reads, _ }
import uk.gov.hmrc.twowaymessage.model.FormId.FormId
import uk.gov.hmrc.twowaymessage.model.MessageType.MessageType

import java.time.format.DateTimeFormatter
import java.util.Base64
import scala.util.Try

object MessageFormat {

  def decodeBase64String(input: String): String =
    new String(Base64.getDecoder.decode(input.getBytes("UTF-8")))

  implicit val dateFormat: Format[LocalDate] = {
    val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dateWrites: Writes[LocalDate] = new Writes[LocalDate] {
      def writes(localDate: LocalDate): JsValue = JsString(localDate.format(datePattern))
    }

    val dateReads: Reads[LocalDate] = new Reads[LocalDate] {
      override def reads(json: JsValue): JsResult[LocalDate] =
        Try(JsSuccess(LocalDate.parse(json.as[String], datePattern), JsPath)).getOrElse(JsError())
    }
    Format[LocalDate](dateReads, dateWrites)
  }

  implicit val formIdFormat: Format[FormId] =
    Format(
      Reads.enumNameReads(FormId),
      Writes.enumNameWrites
    )

  implicit val messageTypeFormat: Format[MessageType] =
    Format(
      Reads.enumNameReads(MessageType),
      Writes.enumNameWrites
    )

  implicit val conversationItemDetailsFormat: Format[ConversationItemDetails] = Json.format[ConversationItemDetails]

  implicit val conversationItemWrites: Writes[ConversationItem] = Json.writes[ConversationItem]

  implicit val conversationItemReads: Reads[ConversationItem] = (
    (__ \ "id").read[String] and
      (__ \ "subject").read[String] and
      (__ \ "body").readNullable[ConversationItemDetails] and
      (__ \ "validFrom").read[LocalDate] and
      (__ \ "content").readNullable[String]
  ) { (id, subject, body, validFrom, content) =>
    ConversationItem(id, subject, body, validFrom, content.map(content => decodeBase64String(content)))
  }
}

object FormId extends Enumeration {

  type FormId = Value

  val Question = Value("2WSM-question")
  val Reply = Value("2WSM-reply")
}

object MessageType extends Enumeration {

  type MessageType = Value

  val Adviser = Value("2wsm-advisor")
  val Customer = Value("2wsm-customer")
}

case class Adviser(pidId: String)

object Adviser {
  implicit val adviserFormat: Format[Adviser] = Json.format[Adviser]
}

case class ConversationItemDetails(
  `type`: MessageType,
  form: FormId,
  issueDate: Option[LocalDate],
  replyTo: Option[String] = None,
  enquiryType: Option[String] = None,
  adviser: Option[Adviser] = None)

case class ConversationItem(
  id: String,
  subject: String,
  body: Option[ConversationItemDetails],
  validFrom: LocalDate,
  content: Option[String]
)

case class ItemMetadata(
  isLatestMessage: Boolean,
  hasLink: Boolean = true,
  hasSmallSubject: Boolean = false
)
