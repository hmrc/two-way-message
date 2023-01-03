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

package uk.gov.hmrc.twowaymessage.connectors

import com.google.inject.Inject
import play.api.http.Status
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient, HttpResponse }
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import scala.concurrent.{ ExecutionContext, Future }
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[MessageConnectorImpl])
trait MessageConnector {
  def getMessages(messageId: String)(implicit hc: HeaderCarrier): Future[HttpResponse]
}
class MessageConnectorImpl @Inject()(httpClient: HttpClient, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext)
  extends MessageConnector with Status {

  private val messageBaseUrl: String = servicesConfig.baseUrl("message")

  def getMessages(messageId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.GET(s"$messageBaseUrl/messages-list/$messageId")
}
