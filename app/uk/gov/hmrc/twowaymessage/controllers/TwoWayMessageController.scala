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

package uk.gov.hmrc.twowaymessage.controllers

import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{ Action, _ }
import uk.gov.hmrc.auth.core.AuthProvider.{ GovernmentGateway, PrivilegedApplication, Verify }
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{ Name, ~ }
import uk.gov.hmrc.domain.TaxIds.TaxIdWithName
import uk.gov.hmrc.gform.dms.DmsMetadata
import uk.gov.hmrc.gform.gformbackend.GformConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithJsonBody
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.twowaymessage.connectors.AuthIdentifiersConnector
import uk.gov.hmrc.twowaymessage.enquiries.{ Enquiry, SubmissionDetails }
import uk.gov.hmrc.twowaymessage.model.MessageFormat._
import uk.gov.hmrc.twowaymessage.model.MessageMetadataFormat._
import uk.gov.hmrc.twowaymessage.model.TwoWayMessageFormat._
import uk.gov.hmrc.twowaymessage.model._
import uk.gov.hmrc.twowaymessage.services.{ HtmlCreatorService, RenderType, TwoWayMessageService }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class TwoWayMessageController @Inject()(
  twms: TwoWayMessageService,
  val authConnector: AuthConnector,
  val gformConnector: GformConnector,
  authIdentifiersConnector: AuthIdentifiersConnector,
  val enquiries: Enquiry,
  val htmlCreatorService: HtmlCreatorService)(implicit ec: ExecutionContext)
    extends InjectedController with WithJsonBody with AuthorisedFunctions {

  // Customer creating a two-way message
  def createMessage(enquiryType: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    authorised().retrieve(Retrievals.allEnrolments and Retrievals.name) {
      case enrolments ~ Some(name) =>
        authIdentifiersConnector
          .enquiryTaxId(enrolments, enquiryType)
          .map { taxId =>
            validateAndPostMessage(enquiryType, taxId, request.body, name)
          }
          .getOrElse(
            Future.successful(Forbidden(Json.toJson("Not authenticated")))
          )
      case _ =>
        Logger.info("Unexpected auth retrievals.")
        Future.successful(Forbidden(Json.toJson("Not authenticated")))
    } recover handleError
  }

  def getRecipientMetadata(messageId: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    twms.getMessageMetadata(messageId).map {
      case Some(m) => Ok(Json.toJson(m))
      case None    => NotFound
    }
  }

  def getRecipientMessageContentBy(messageId: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    twms.getMessageContentBy(messageId).map {
      case Some(m) => Ok(m)
      case None    => NotFound
    }
  }

  def getMessagesListBy(messageId: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    twms.findMessagesBy(messageId).map {
      case Right(messages) => Ok(Json.toJson(messages))
      case Left(errors)    => BadRequest(Json.obj("error" -> 400, "message" -> errors))
    }
  }

  def getMessagesListSizeBy(messagesId: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    twms.findMessagesBy(messagesId).map {
      case Right(messages) => Ok(JsNumber(messages.size))
      case Left(errors)    => BadRequest(Json.obj("error" -> 400, "message" -> errors))
    }
  }

  def handleError(): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Logger.debug("Request did not have an Active Session, returning Unauthorised - Unauthenticated Error")
      Unauthorized(Json.toJson("Not authenticated"))
    case _: AuthorisationException =>
      Logger.debug("Request has an active session but was not authorised, returning Forbidden - Not Authorised Error")
      Forbidden(Json.toJson("Not authorised"))
    case e: Exception =>
      Logger.error(s"Unknown error: ${e.toString}")
      InternalServerError
  }

  // Validates the customer's message payload and then posts the message
  def validateAndPostMessage(enquiryType: String, taxIdentifier: TaxIdWithName, requestBody: JsValue, name: Name)(
    implicit hc: HeaderCarrier): Future[Result] =
    requestBody.validate[TwoWayMessage] match {
      case _: JsSuccess[_] =>
        enquiries(enquiryType) match {
          case Some(enquiryId) =>
            val dmsMetaData =
              DmsMetadata(
                enquiryId.dmsFormId,
                taxIdentifier.value,
                enquiryId.classificationType,
                enquiryId.businessArea)
            twms.post(enquiryId, taxIdentifier, requestBody.as[TwoWayMessage], dmsMetaData, name)
          case None =>
            Future.successful(BadRequest(Json.obj("error" -> 400, "message" -> s"Invalid EnquityId: $enquiryType")))
        }

      case e: JsError => Future.successful(BadRequest(Json.obj("error" -> 400, "message" -> JsError.toJson(e))))
    }

  // Adviser replying to a customer message
  def createAdviserResponse(replyTo: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    {
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      authorised(AuthProviders(PrivilegedApplication)) {
        validateAndPostAdviserResponse(request.body, replyTo)
      }.recoverWith {
        case _ => Future.successful(Forbidden)
      }
    }
  }

  // Validates the adviser response payload and then posts the reply
  def validateAndPostAdviserResponse(requestBody: JsValue, replyTo: String)(
    implicit hc: HeaderCarrier): Future[Result] =
    requestBody.validate[TwoWayMessageReply] match {
      case _: JsSuccess[_] => twms.postAdviserReply(requestBody.as[TwoWayMessageReply], replyTo)
      case e: JsError      => Future.successful(BadRequest(Json.obj("error" -> 400, "message" -> JsError.toJson(e))))
    }

  // Customer replying to an adviser's message
  // TODO: queueId is redundant, as it's currently fetched from the original message metadata (i.e. postCustomerReply/getMessageMetadata) - you can safely remove
  def createCustomerResponse(enquiryType: String, replyTo: String): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      authorised().retrieve(Retrievals.allEnrolments) { enrolments =>
        authIdentifiersConnector
          .enquiryTaxId(enrolments, enquiryType)
          .map { taxId =>
            validateAndPostCustomerResponse(request.body, replyTo)
          }
          .getOrElse(
            Future.successful(Forbidden(Json.toJson("Not authenticated")))
          )
      } recover handleError
  }

  // Validates the customer's response payload and then posts the reply
  def validateAndPostCustomerResponse(requestBody: JsValue, replyTo: String)(
    implicit hc: HeaderCarrier): Future[Result] =
    requestBody.validate[TwoWayMessageReply] match {
      case _: JsSuccess[_] => twms.postCustomerReply(requestBody.as[TwoWayMessageReply], replyTo)
      case e: JsError      => Future.successful(BadRequest(Json.obj("error" -> 400, "message" -> JsError.toJson(e))))
    }

  def getCurrentResponseTime(formType: String): Action[AnyContent] = Action.async { implicit request =>
    enquiries(formType) match {
      case Some(form) => Future.successful(Ok(Json.obj("responseTime" -> form.responseTime)))
      case _          => Future.successful(NotFound)
    }
  }

  def getEnquiryTypeDetails(enquiryTypeString: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    authorised().retrieve(Retrievals.allEnrolments) {
      case enrolments =>
        authIdentifiersConnector
          .enquiryTaxId(enrolments, enquiryTypeString)
          .map { taxIdWithName =>
            enquiries(enquiryTypeString).map(enquiry =>
              SubmissionDetails(enquiry.displayName, enquiry.responseTime, taxIdWithName.name, taxIdWithName.value)) match {
              case Some(enquiryType) => Future.successful(Ok(Json.toJson(enquiryType)))
              case _                 => Future.successful(NotFound)
            }
          }
          .getOrElse(
            Future.successful(Forbidden(Json.toJson("Not authenticated")))
          )
      case _ =>
        Logger.info("Unexpected auth retrievals.")
        Future.successful(Forbidden(Json.toJson("Not authenticated")))
    } recover handleError
  }

  def getContentBy(id: String, msgType: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(AuthProviders(GovernmentGateway, PrivilegedApplication, Verify)) {

      def createMsg(typ: RenderType.ReplyType): Future[Result] =
        twms.findMessagesBy(id).flatMap {
          case Right(msgList) => getHtmlResponse(id, msgList, typ)
          case Left(err) =>
            Logger.warn(s"Error retrieving messages: $err")
            Future.successful(BadGateway(err))
        }

      msgType match {
        case "Customer" => createMsg(RenderType.CustomerLink)
        case "Adviser"  => createMsg(RenderType.Adviser)
        case _          => Future.successful(BadRequest)
      }

    } recover handleError
  }

  private def getHtmlResponse(id: String, msgList: List[ConversationItem], typ: RenderType.ReplyType): Future[Result] =
    htmlCreatorService.createConversation(id, msgList, typ).map {
      case Right(html) => Ok(html)
      case Left(error) =>
        Logger.warn(s"HtmlCreatorService conversion error: $error")
        InternalServerError(error)
    }

  def getLatestMessage(messageId: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    twms.getLastestMessage(messageId).map {
      case Left(error) => BadGateway(error)
      case Right(html) => Ok(html)
    }
  }

  def getPreviousMessages(messageId: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    twms.getPreviousMessages(messageId).map {
      case Left(error) => BadGateway(error)
      case Right(html) => Ok(html)
    }
  }
}
