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

package uk.gov.hmrc.twowaymessage.enquiries.repos

import play.api.Play
import play.api.libs.json.{Json, Writes}
//import uk.gov.hmrc.twowaymessage.enquiries.repos.P800.dmsFormId

object DMSClassificationTypes {
  val PSA_DFS_Secure_Messaging_SA = "PSA-DFS Secure Messaging SA"
}
object DMSBusinessArea {
  val PT_Operations = "PT Operations"
}
object DisplayNames {
  val P800 = "asdfasdfasdfasdf"
}

object EnquiryTypeFormat {
  implicit val enquiryTypeWrites: Writes[EnquiryType] = Json.writes[EnquiryType]

}

case class EnquiryType (
  val title: String,
  val dmsFormId: String,
  val classificationType: String,
  val businessArea: String,
  val responseTime: String,
  val displayName: String
)

object EnquiryTypes  {

  val P800 = EnquiryType(
    title = "P800" ,
    dmsFormId = "P800",
    classificationType = DMSClassificationTypes.PSA_DFS_Secure_Messaging_SA,
    businessArea = DMSBusinessArea.PT_Operations,
    responseTime = Play.current.configuration.getString("forms.p800.responseTime").get,
    displayName = DisplayNames.P800
  )
   val P800OverPayment = EnquiryType(
        title = "P800" ,
        dmsFormId = "P800",
        classificationType = DMSClassificationTypes.PSA_DFS_Secure_Messaging_SA,
        businessArea = DMSBusinessArea.PT_Operations,
        responseTime = Play.current.configuration.getString("forms.p800.responseTime").get,
        displayName = DisplayNames.P800
    )
}
