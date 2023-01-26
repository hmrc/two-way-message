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

package uk.gov.hmrc.twowaymessage.model

import org.joda.time.LocalDate
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{ Json, _ }
import uk.gov.hmrc.twowaymessage.assets.Fixtures
import uk.gov.hmrc.twowaymessage.model.MessageFormat._

class MessageFormatSpec extends AnyWordSpec with Fixtures with Matchers {

  "Message json reader" should {
    "read conversation item as defined in message microservice" in {
      val json = Json.parse(conversationItem("5d02201b5b0000360151779e"))
      val messageResult = json.validate[ConversationItem]
      messageResult.isSuccess shouldBe true
      messageResult.get.validFrom.toString should be("2013-12-01")
      messageResult.get.body.get.`type` should be(MessageType.Adviser)
    }

    "read conversation items as defined in message microservice" in {
      val id1 = "5d02201b5b0000360151779e"
      val id2 = "5d021fbe5b0000200151779c"
      val json = Json.parse(conversationItems(id1, id2))
      val messageResult = json.validate[List[ConversationItem]]
      messageResult.isSuccess shouldBe true
      messageResult.get.map { _.id } should contain allOf ("5d02201b5b0000360151779e", "5d021fbe5b0000200151779c")
    }
  }

  "ConversationItem" should {
    "content should be successfully decoded" in {
      val item = Json.parse(conversationItem("5d02201b5b0000360151779e")).validate[ConversationItem]
      item shouldBe JsSuccess(
        ConversationItem(
          id = "5d02201b5b0000360151779e",
          subject = "Matt Test 1",
          body = Some(
            ConversationItemDetails(
              MessageType.Adviser,
              FormId.Reply,
              Some(LocalDate.parse("2019-06-13")),
              Some("5d021fbe5b0000200151779c"),
              Some("p800"),
              Some(Adviser("123"))
            )
          ),
          validFrom = LocalDate.parse("2013-12-01"),
          content = Some(
            "Dear TestUser Thank you for your message of 13 June 2019.<br>To recap your question, I think you're " +
              "asking for help with<br>I believe this answers your question and hope you are satisfied with the " +
              "response.<br>If you think there is something important missing, use the link at the end of this " +
              "message to find out how to contact HMRC.<br>Regards<br>Matthew Groom<br>nHMRC digital team."
          )
        )
      )
    }
  }
}
