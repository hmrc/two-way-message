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

import com.google.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AdviserResponseMap @Inject()(val servicesConfig: ServicesConfig) {

  val adviserResponseMap = Map(
    "P800" -> servicesConfig.getConfString("form.responseTime.P800", "7 working days")
  )

  def getResponseTimeForForm(form: String): String = {
    adviserResponseMap(form.toUpperCase())
  }
}
