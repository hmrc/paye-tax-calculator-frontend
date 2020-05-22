/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.{SalaryInHoursFormProvider, TaxResult, UserTaxCode}
import javax.inject.{Inject, Singleton}
import models.QuickCalcAggregateInput
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents, Request, Result}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.ActionWithSessionId
import views.html.pages.{HoursAWeekView, ResultView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShowResultsController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  resultView: ResultView
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  def showResult(): Action[AnyContent] =
    salaryRequired(cache, implicit request =>
      aggregate =>
        if (aggregate.allQuestionsAnswered) {
          Ok(resultView(TaxResult.taxCalculation(aggregate), UserTaxCode.startOfCurrentTaxYear))
        } else Redirect(navigator.redirectToNotYetDonePage(aggregate))
    )

  private def salaryRequired[T](
                                 cache: QuickCalcCache,
                                 furtherAction: Request[AnyContent] => QuickCalcAggregateInput => Result
                               ): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

      cache.fetchAndGetEntry().map {
        case Some(aggregate) =>
          if (aggregate.savedSalary.isDefined)
            furtherAction(request)(aggregate)
          else
            Redirect(routes.SalaryController.showSalaryForm())
        case None =>
          Redirect(routes.SalaryController.showSalaryForm())
      }
    }


}