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

package uk.gov.hmrc.twowaymessage.enquiries

import org.scalatest.{Matchers, WordSpec}

class EnquirySpec extends WordSpec with Matchers {

  "for a P800 enquiry" should {

    "find a name for 'P800'" in {
      Enquiry("p800") match {
        case None => fail("Invalid enquiry")
        case Some(meteadata) => meteadata.title shouldBe ("P800")
      }
    }

    "Case insensitive lookup for 'P800'" in {
      Enquiry("P800") match {
        case None => fail("Invalid enquiry key")
        case Some(meteadata) => meteadata.businessArea shouldBe ("PT Operations")
      }
    }

    "find a classificationType for 'P800'" in {
      Enquiry("p800") match {
        case None => fail("Invalid enquiry key")
        case Some(meteadata) => meteadata.classificationType shouldBe ("PSA-DFS Secure Messaging SA")
      }
    }
  }


  "for an invalid enquiry" should {
    "find an invalid name " in {
      Enquiry("badQueue") match {
        case None =>
        case Some(meteadata) => fail("Found enquiry")
      }
    }
  }

}
