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
import forms.TaxResult

import javax.inject.{Inject, Singleton}
import models.{QuickCalcAggregateInput, UserTaxCode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, DefaultTaxCodeProvider}
import views.html.pages.ResultView

import scala.concurrent.ExecutionContext

@Singleton
class ShowResultsController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  resultView: ResultView,
  defaultTaxCodeProvider: DefaultTaxCodeProvider
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent
  def isUkOrScottishTaxCode(aggregateInput: QuickCalcAggregateInput): Boolean = {
    aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultUkTaxCode)
      || aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultScottishTaxCode)))
  }

  def salaryOverHundredThousand(aggregateInput: QuickCalcAggregateInput): Boolean = {
    aggregateInput.savedSalary.exists(_.amount >= 100002)
  }

  def salaryCheck(aggregateInput: QuickCalcAggregateInput) : Boolean = {
    if(salaryOverHundredThousand(aggregateInput) && isUkOrScottishTaxCode(aggregateInput)){
      true
    } else {
      false
    }
  }

  def showResult(): Action[AnyContent] =
    salaryRequired(cache, implicit request =>
      aggregate =>
        if (aggregate.allQuestionsAnswered) {
          val isScottish = if(aggregate.savedTaxCode.exists(_.taxCode.exists(_.contains("S")))){
            true
          } else if(aggregate.savedScottishRate.exists(_.payScottishRate)) {
            true
          } else {
            false
          }
          Ok(resultView(TaxResult.taxCalculation(aggregate, defaultTaxCodeProvider), defaultTaxCodeProvider.startOfCurrentTaxYear, isScottish, salaryCheck(aggregate)))
        } else Redirect(navigator.redirectToNotYetDonePage(aggregate))
    )

  private def salaryRequired[T](
                                 cache: QuickCalcCache,
                                 furtherAction: Request[AnyContent] => QuickCalcAggregateInput => Result
                               ): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      cache.fetchAndGetEntry().map {
        case Some(aggregate) =>
          if (aggregate.savedSalary.isDefined)
            furtherAction(request)(aggregate)
          else
            Redirect(routes.SalaryController.showSalaryForm)
        case None =>
          Redirect(routes.SalaryController.showSalaryForm)
      }
    }


}
