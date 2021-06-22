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

package uk.gov.hmrc.twowaymessage.connectors

import uk.gov.hmrc.auth.core
import uk.gov.hmrc.auth.core.{ Nino => _, _ }
import uk.gov.hmrc.domain.TaxIds.TaxIdWithName
import uk.gov.hmrc.domain._
import uk.gov.hmrc.twowaymessage.enquiries.Enquiry

import javax.inject.{ Inject, Singleton }

@Singleton
class AuthIdentifiersConnector @Inject()(
  val authConnector: core.AuthConnector,
  val enquiries: Enquiry
) extends AuthorisedFunctions {

  def enquiryTaxId(enrolments: Enrolments, enquiryTypeString: String): Option[TaxIdWithName] =
    enquiryTypeString match {
      case "sa-general" =>
        enrolments.enrolments.collectFirst {
          case Enrolment("IR-SA", Seq(identifier), "Activated", _) => SaUtr(identifier.value)
        }
      case "ct-general" =>
        enrolments.enrolments.collectFirst {
          case Enrolment("IR-CT", Seq(identifier), "Activated", _) => CtUtr(identifier.value)
        }
      case "vat-general" =>
        enrolments.enrolments
          .collectFirst {
            case Enrolment("HMRC-MTD-VAT", Seq(identifier), "Activated", _) => HmrcMtdVat(identifier.value)
          } orElse enrolments.enrolments.collectFirst {
          case Enrolment("HMCE-VATDEC-ORG", Seq(identifier), "Activated", _) =>
            new TaxIdentifier with SimpleName {
              override val name: String = "HMCE-VATDEC-ORG"
              override def value: String = identifier.value
            }
        }
      case "epaye-general" =>
        enrolments.enrolments.collectFirst {
          case Enrolment(
              "IR-PAYE",
              Seq(
                EnrolmentIdentifier("TaxOfficeNumber", officeNum),
                EnrolmentIdentifier("TaxOfficeReference", officeRef)
              ),
              "Activated",
              _
              ) =>
            new TaxIdentifier with SimpleName {
              override val name: String = "empRef"
              override def value: String = EmpRef(officeNum, officeRef).value
            }
        }

      case "epaye-jrs" =>
        enrolments.enrolments.collectFirst {
          case Enrolment(
              "IR-PAYE",
              Seq(
                EnrolmentIdentifier("TaxOfficeNumber", officeNum),
                EnrolmentIdentifier("TaxOfficeReference", officeRef)
              ),
              "Activated",
              _
              ) =>
            new TaxIdentifier with SimpleName {
              override val name: String = "empRef"
              override def value: String = EmpRef(officeNum, officeRef).value
            }
        }

      case _ =>
        enrolments.enrolments.collectFirst {
          case Enrolment("HMRC-NI", Seq(identifier), "Activated", _) => Nino(identifier.value)
        }
    }
}
