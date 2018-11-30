/*
 * Copyright 2018 HM Revenue & Customs
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

import java.util.UUID
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.twowaymessage.connectors.MessageConnector
import uk.gov.hmrc.twowaymessage.model.CommonFormats._
import uk.gov.hmrc.twowaymessage.model.Error
import uk.gov.hmrc.twowaymessage.model._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class TwoWayMessageService @Inject()(messageConnector: MessageConnector)(implicit ec: ExecutionContext) {

  implicit val hc = HeaderCarrier()

  def createJsonBody(twoWayMessage: TwoWayMessage): Message = {
    Message(
      ExternalRef(
        randomUUID.toString(),
        "2WSM-CUSTOMER"
        ),
      twoWayMessage.recipient,
      "2WSM-customer",
      twoWayMessage.subject,
      twoWayMessage.content.getOrElse(""),
      Details(
        "2WSM-question"
      )
    )
  }

  def post(twoWayMessage: TwoWayMessage): Future[Result] = {
    val body = createJsonBody(twoWayMessage)
    messageConnector.postMessage(body) map (
      response =>
        response.status match {
          case OK => Created(Json.toJson("id" -> body.externalRef.id))
          case _ => BadGateway(Json.toJson(Error(response.status,response.body)))
      })
  }

  def post(twoWayMessage: TwoWayMessageReply): Future[Result] = {
    val originalMessageEmail = blockingGetEmailAddressForId(twoWayMessage)
    val body = createJsonBodyForReply(twoWayMessage, originalMessageEmail)
    messageConnector.postMessage(body) map (response =>
      response.status match {
        case OK => Ok(Json.toJson("id" -> UUID.randomUUID().toString))
        case _ => BadGateway(Json.toJson(Error(response.status,response.body)))
      })
  }

  def blockingGetEmailAddressForId(twoWayMessageReply: TwoWayMessageReply): String = {
    val originalMessageEmail = messageConnector.validateAndGetEmailAddress(twoWayMessageReply.replyTo)
    Await.result(originalMessageEmail, Duration.apply(20, TimeUnit.SECONDS)).body
  }

  def createJsonBodyForReply(twoWayMessageReply: TwoWayMessageReply, originalEmail: String): Message = {
    Message(
      ExternalRef(
        randomUUID.toString(),
        "2WSM-ADVISOR"
      ),
      twoWayMessageReply.recipient,
      "2wsm-advisor",
      twoWayMessageReply.subject,
      twoWayMessageReply.content.getOrElse(""),
      Details(
        "2WSM-question",
        Option.apply(twoWayMessageReply.replyTo)
      ),
      email=Option.apply(originalEmail)
    )
  }
}
