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

package controllers

import config.AppConfig

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import services.QuickCalcCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.ActionWithSessionId
import views.html.pages.ResetView

import scala.concurrent.{ExecutionContext, Future}

class ResetController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  resetView:                     ResetView
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {
  override def parser: BodyParser[AnyContent] = parse.anyContent

  def reset(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

    cache.fetchAndGetEntry().flatMap {
      case Some(aggregate) =>
        val updatedAggregate = aggregate.copy(None, None, None, None, None)
        cache.save(updatedAggregate).map(_ => Ok(resetView()))
      case None =>
        Future.successful(Ok(resetView()))
    }
  }
}
