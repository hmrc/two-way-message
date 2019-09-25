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

object DMSClassificationTypes {
  val PSA_DFS_Secure_Messaging_SA = "PSA-DFS Secure Messaging SA"
}
object DMSBusinessArea {
  val PT_Operations = "PT Operations"
}
object DisplayNames {
  val P800 = "asdfasdfasdfasdf"
}

sealed trait EnquiryType {
  val title: String
  val dmsFormId: String
  val classificationType: String
  val businessArea: String
  val responseTime: String
  val displayName: String
}

case object P800 extends EnquiryType {
  val title: String = "P800"
  val dmsFormId: String = "P800"
  val classificationType: String = DMSClassificationTypes.PSA_DFS_Secure_Messaging_SA
  val businessArea: String = DMSBusinessArea.PT_Operations
  lazy val responseTime: String = Play.current.configuration.getString("forms.p800.responseTime").get
  val displayName = DisplayNames.P800
}

case object P800OverPayment extends EnquiryType {
    val title: String = "P800"
    val dmsFormId: String = "P800"
    val classificationType: String = DMSClassificationTypes.PSA_DFS_Secure_Messaging_SA
    val businessArea: String = DMSBusinessArea.PT_Operations
    lazy val responseTime: String = Play.current.configuration.getString("forms.p800.responseTime").get
    val displayName = DisplayNames.P800
}
