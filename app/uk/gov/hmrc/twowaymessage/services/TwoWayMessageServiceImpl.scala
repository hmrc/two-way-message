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

package uk.gov.hmrc.twowaymessage.services

import com.google.inject.Inject
import play.api.http.Status.OK
import play.api.libs.json.{ JsError, Json }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.twowaymessage.connectors.MessageConnector
import uk.gov.hmrc.twowaymessage.model.MessageFormat._
import uk.gov.hmrc.twowaymessage.model._

import scala.concurrent.{ ExecutionContext, Future }

class TwoWayMessageServiceImpl @Inject()(messageConnector: MessageConnector)(implicit ec: ExecutionContext)
    extends TwoWayMessageService {

  override def findMessagesBy(messageId: String)(
    implicit hc: HeaderCarrier): Future[Either[String, List[ConversationItem]]] =
    messageConnector.getMessages(messageId).flatMap { response =>
      response.status match {
        case OK =>
          response.json
            .validate[List[ConversationItem]]
            .fold(
              errors => Future.successful(Left(Json stringify JsError.toJson(errors))),
              msgList => Future.successful(Right(msgList))
            )
        case _ => Future.successful(Left("Error retrieving messages"))
      }
    }
}
