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

  "for a 'P800' enquiry" should {

    "find a name for 'P800'" in {
      Enquiry("p800") match {
        case None => fail("Invalid enquiry")
        case Some(meteadata) => meteadata.name shouldBe ("p800")
      }
    }

    "Case insensitive lookup for 'P800'" in {
      Enquiry("P800") match {
        case None => fail("Invalid enquiry key")
        case Some(meteadata) => meteadata.businessArea shouldBe ("PT Operations")
      }
    }

    "find a businessArea for 'P800'" in {
      Enquiry("p800") match {
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

  "for a p800-overpayment enquiry" should {
    "find a name" in {
      Enquiry("p800-overpayment").get.name shouldBe "p800-overpayment"
    }
    "find dmsFormId" in {
      Enquiry("p800-overpayment").get.dmsFormId shouldBe "P800"
    }

    "find classificationType" in {
      Enquiry("p800-overpayment").get.classificationType shouldBe "PSA-DFS Secure Messaging SA"
    }

    "find businessArea" in {
      Enquiry("p800-overpayment").get.businessArea shouldBe "PT Operations"
    }
  }

  "for a p800-paid enquiry" should {
    "find a name" in {
      Enquiry("p800-paid").get.name shouldBe "p800-paid"
    }
    "find dmsFormId" in {
      Enquiry("p800-paid").get.dmsFormId shouldBe "P800"
    }

    "find classificationType" in {
      Enquiry("p800-paid").get.classificationType shouldBe "PSA-DFS Secure Messaging SA"
    }

    "find businessArea" in {
      Enquiry("p800-paid").get.businessArea shouldBe "PT Operations"
    }

  }

  "p800-processing enquiry" should {
    "find a name" in {
      Enquiry("p800-processing").get.name shouldBe "p800-processing"
    }
    "find dmsFormId" in {
      Enquiry("p800-processing").get.dmsFormId shouldBe "P800"
    }

    "find classificationType" in {
      Enquiry("p800-processing").get.classificationType shouldBe "PSA-DFS Secure Messaging SA"
    }

    "find businessArea" in {
      Enquiry("p800-processing").get.businessArea shouldBe "PT Operations"
    }

  }

  "p800-sent enquiry" should {
    "find a name" in {
      Enquiry("p800-sent").get.name shouldBe "p800-sent"
    }
    "find dmsFormId" in {
      Enquiry("p800-sent").get.dmsFormId shouldBe "P800"
    }

    "find classificationType" in {
      Enquiry("p800-sent").get.classificationType shouldBe "PSA-DFS Secure Messaging SA"
    }

    "find businessArea" in {
      Enquiry("p800-sent").get.businessArea shouldBe "PT Operations"
    }

  }

  "p800-not-available enquiry" should {
    "find a name" in {
      Enquiry("p800-not-available").get.name shouldBe "p800-not-available"
    }
    "find dmsFormId" in {
      Enquiry("p800-not-available").get.dmsFormId shouldBe "P800"
    }

    "find classificationType" in {
      Enquiry("p800-not-available").get.classificationType shouldBe "PSA-DFS Secure Messaging SA"
    }

    "find businessArea" in {
      Enquiry("p800-not-available").get.businessArea shouldBe "PT Operations"
    }

  }

  "p800-underpayment enquiry" should {
    "find a name" in {
      Enquiry("p800-underpayment").get.name shouldBe "p800-underpayment"
    }
    "find dmsFormId" in {
      Enquiry("p800-underpayment").get.dmsFormId shouldBe "P800"
    }

    "find classificationType" in {
      Enquiry("p800-underpayment").get.classificationType shouldBe "PSA-DFS Secure Messaging SA"
    }

    "find businessArea" in {
      Enquiry("p800-underpayment").get.businessArea shouldBe "PT Operations"
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
