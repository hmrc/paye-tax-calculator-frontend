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

package controllers

import config.AppConfig

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ControllerComponents, *}
import services.QuickCalcCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.ActionWithSessionId

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class QuickCalcController @Inject() (
  override val messagesApi: MessagesApi,
  cache: QuickCalcCache,
  val controllerComponents: ControllerComponents
)(implicit val appConfig: AppConfig, val executionContext: ExecutionContext)
    extends BackendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  def redirectToSalaryForm(): Action[AnyContent] = validateAcceptWithSessionId().async {
    Future.successful(Redirect(routes.SalaryController.showSalaryForm()))
  }

  def restartQuickCalc(): Action[AnyContent] = validateAcceptWithSessionId().async { implicit request =>
    implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

    cache.fetchAndGetEntry().flatMap {
      case Some(aggregate) =>
        val updatedAggregate = aggregate.copy(None, None, None, None, None, None, None, None)
        cache.save(updatedAggregate).map(_ => Redirect(routes.SalaryController.showSalaryForm()))
      case None =>
        Future.successful(Redirect(routes.SalaryController.showSalaryForm()))
    }
  }

}
