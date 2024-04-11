/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Logging
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.{ GovernmentGateway, PrivilegedApplication }
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.twowaymessage.model._
import uk.gov.hmrc.twowaymessage.services.{ HtmlCreatorService, RenderType, TwoWayMessageService }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class TwoWayMessageController @Inject() (
  twms: TwoWayMessageService,
  val authConnector: AuthConnector,
  val htmlCreatorService: HtmlCreatorService
)(implicit ec: ExecutionContext)
    extends InjectedController with AuthorisedFunctions with Logging {

  def getContentBy(id: String, msgType: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(AuthProviders(GovernmentGateway, PrivilegedApplication)) {

      def createMsg(replyType: RenderType.ReplyType): Future[Result] =
        twms.findMessagesBy(id).flatMap {
          case Right(msgList) => getHtmlResponse(id, msgList, replyType)
          case Left(err) =>
            logger.warn(s"Error retrieving messages: $err")
            Future.successful(BadGateway(err))
        }

      msgType match {
        case "Customer" => createMsg(RenderType.CustomerLink)
        case "Adviser"  => createMsg(RenderType.Adviser)
        case _          => Future.successful(BadRequest)
      }

    } recover handleError
  }

  private def getHtmlResponse(
    id: String,
    msgList: List[ConversationItem],
    replyType: RenderType.ReplyType
  ): Future[Result] =
    htmlCreatorService.createConversation(id, msgList, replyType).map {
      case Right(html) => Ok(html)
      case Left(error) =>
        logger.warn(s"HtmlCreatorService conversion error: $error")
        InternalServerError(error)
    }

  private def handleError: PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      logger.debug("Request did not have an Active Session, returning Unauthorised - Unauthenticated Error")
      Unauthorized(Json.toJson("Not authenticated"))
    case _: AuthorisationException =>
      logger.debug("Request has an active session but was not authorised, returning Forbidden - Not Authorised Error")
      Forbidden(Json.toJson("Not authorised"))
    case e: Exception =>
      logger.error(s"Unknown error: ${e.toString}")
      InternalServerError
  }
}
