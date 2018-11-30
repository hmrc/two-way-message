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

package uk.gov.hmrc.twowaymessage.controllers

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.WithJsonBody
import uk.gov.hmrc.twowaymessage.model.TwoWayMessageFormat._
import uk.gov.hmrc.twowaymessage.model.{TwoWayMessage, TwoWayMessageReply}
import uk.gov.hmrc.twowaymessage.services.TwoWayMessageService

import scala.concurrent.{ExecutionContext, Future}

class TwoWayMessageController @Inject()(twms: TwoWayMessageService)
                                       (implicit ec: ExecutionContext) extends InjectedController with WithJsonBody {

  private val logger = Logger(this.getClass)

  def createMessage(queueId: String): Action[JsValue] = Action.async(parse.json) {
    logger.info("Queue ID:" + queueId)
    implicit request =>
      validateOriginalMessage(request.body)
  }

  def validateOriginalMessage(requestBody: JsValue): Future[Result] = {
    requestBody.validate[TwoWayMessage] match {
      case s: JsSuccess[_] => twms.post(requestBody.as[TwoWayMessage])
      case e: JsError => Future.successful(BadRequest(Json.obj("error" -> "OK", "message" -> JsError.toJson(e))))
    }
  }

  def createAdvisorResponse(replyTo: String): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      validateReplyMessage(request.body)
  }

  def validateReplyMessage(requestBody: JsValue): Future[Result] = {
    requestBody.validate[TwoWayMessageReply] match {
      case s: JsSuccess[_] => twms.post(requestBody.as[TwoWayMessageReply])
      case e: JsError => Future.successful(BadRequest(Json.obj("error" -> "OK", "message" -> JsError.toJson(e))))
    }
  }
}
