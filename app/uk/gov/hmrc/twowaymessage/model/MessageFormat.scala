/*
 * Copyright 2021 HM Revenue & Customs
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

import org.apache.commons.codec.binary.Base64
import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JodaReads, JodaWrites, Json, Reads, _ }
import uk.gov.hmrc.domain.TaxIds.TaxIdWithName
import uk.gov.hmrc.domain._
import uk.gov.hmrc.twowaymessage.model.FormId.FormId
import uk.gov.hmrc.twowaymessage.model.MessageType.MessageType

object MessageFormat {

  def decodeBase64String(input: String): String =
    new String(Base64.decodeBase64(input.getBytes("UTF-8")))

  implicit val taxpayerNameFormat: Format[TaxpayerName] = Json.format[TaxpayerName]

  implicit val identifierReads: Reads[TaxIdWithName] =
    ((__ \ "name").readNullable[String] and (__ \ "value").readNullable[String]).tupled
      .flatMap[TaxIdWithName] {
        case (Some("sautr"), Some(value)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(SaUtr(value))
          }
        case (Some("nino"), Some(value)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(Nino(value))
          }
        case (Some("ctutr"), Some(value)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(CtUtr(value))
          }
        case (Some("HMRC-OBTDS-ORG"), Some(value)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(HmrcObtdsOrg(value))
          }
        case (Some("HMRC-MTD-VAT"), Some(value)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(HmrcMtdVat(value))
          }
        case (Some("empRef"), Some(v)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(
              new TaxIdentifier with SimpleName {
                override val name: String = "empRef"
                override def value: String = v
              }
            )
          }
        case (Some("HMCE-VATDEC-ORG"), Some(v)) =>
          Reads[TaxIdWithName] { _ =>
            JsSuccess(
              new TaxIdentifier with SimpleName {
                override val name: String = "HMCE-VATDEC-ORG"
                override def value: String = v
              }
            )
          }
        case (_, None) =>
          Reads[TaxIdWithName] { _ =>
            JsError("recipient.taxIdentifier.name: missing tax identifier")
          }
        case (Some(name), _) =>
          Reads[TaxIdWithName] { _ =>
            JsError(s"recipient.taxIdentifier.name: unknown tax identifier name $name")
          }
        case (None, _) =>
          Reads[TaxIdWithName] { _ =>
            JsError("recipient.taxIdentifier.name: missing tax identifier name")
          }
      }

  implicit val identifierWrites = new Writes[TaxIdWithName] {
    override def writes(taxId: TaxIdWithName): JsValue =
      JsObject(Seq("name" -> JsString(taxId.name), "value" -> JsString(taxId.value)))
  }

  implicit val format: Format[TaxIdWithName] = Format(identifierReads, identifierWrites)

  implicit val recipientFormat: Format[Recipient] = Json.format[Recipient]

  implicit val externalRefFormat: Format[ExternalRef] = Json.format[ExternalRef]

  implicit val dateFormat: Format[LocalDate] =
    Format[LocalDate](JodaReads.jodaLocalDateReads("yyyy-MM-dd"), JodaWrites.jodaLocalDateWrites("yyyy-MM-dd"))

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

  implicit val detailsFormat: Format[Details] = Json.format[Details]

  implicit val conversationItemDetailsFormat: Format[ConversationItemDetails] = Json.format[ConversationItemDetails]

  implicit val messageFormat: Format[Message] = Json.format[Message]

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

case class Recipient(taxIdentifier: TaxIdWithName, email: String, name: Option[TaxpayerName] = Option.empty)

case class Message(
  externalRef: ExternalRef,
  recipient: Recipient,
  messageType: MessageType,
  subject: String,
  content: String,
  details: Details)

case class TaxpayerName(
  title: Option[String] = None,
  forename: Option[String] = None,
  secondForename: Option[String] = None,
  surname: Option[String] = None,
  honours: Option[String] = None,
  line1: Option[String] = None,
  line2: Option[String] = None,
  line3: Option[String] = None)

case class ExternalRef(id: String, source: String)

case class Adviser(pidId: String)
object Adviser {
  implicit val adviserFormat: Format[Adviser] = Json.format[Adviser]
}

case class Details(
  formId: FormId,
  replyTo: Option[String] = None,
  threadId: Option[String] = None,
  enquiryType: Option[String] = None,
  adviser: Option[Adviser] = None,
  waitTime: Option[String] = None,
  topic: Option[String] = None)

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
