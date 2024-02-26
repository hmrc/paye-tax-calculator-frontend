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
import models.QuickCalcAggregateInput
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import play.twirl.api.Html
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import uk.gov.hmrc.time.TaxYear
import utils.{ActionWithSessionId, AggregateConditionsUtil, DefaultTaxCodeProvider}
import views.html.components.link
import views.html.pages.ResultView

import java.time.{LocalDate, ZoneId}
import scala.concurrent.ExecutionContext

@Singleton
class ShowResultsController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  resultView: ResultView,
  defaultTaxCodeProvider: DefaultTaxCodeProvider,
  aggregateConditions: AggregateConditionsUtil,
  link: link
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  def salaryOverHundredThousand(aggregateInput: QuickCalcAggregateInput): Boolean = {
    aggregateInput.savedSalary.exists(_.amount >= 100002)
  }

  def salaryCheck(aggregateInput: QuickCalcAggregateInput) : Boolean = {
    if(salaryOverHundredThousand(aggregateInput) && aggregateConditions.isUkOrScottishTaxCode(aggregateInput)){
      true
    } else {
      false
    }
  }

  private def getCurrentTaxYear: String = {
    val currentDate = LocalDate.now(ZoneId.of("Europe/London"))
    val taxYear = TaxYear(currentDate.getYear)
    if (currentDate isBefore taxYear.starts) {
      val previousTaxYear = taxYear.previous
      s"${previousTaxYear.startYear}/${taxYear.startYear.toString.takeRight(2)}"
    } else {
      s"${taxYear.startYear}/${(taxYear.startYear + 1).toString.takeRight(2)}"
    }
  }

  def sideBarBullets(aggregateInput: QuickCalcAggregateInput)(implicit messages: Messages): Seq[Option[Html]] = {
    Seq(
      Some(Html(Messages("quick_calc.result.sidebar.one_job"))),
      if(aggregateConditions.payScottishRate(aggregateInput) && aggregateConditions.taxCodeContainsS(aggregateInput))
        Some(Html(Messages("quick_calc.result.sidebar.appliedScottishIncomeTaxRates"))) else None,
      if(!aggregateConditions.payScottishRate(aggregateInput) &&
        aggregateConditions.taxCodeContainsS(aggregateInput))
        Some(Html(Messages("quick_calc.result.sidebar.scottish_tax_code"))) else None,
      if(!aggregateConditions.taxCodeContainsS(aggregateInput) && aggregateConditions.payScottishRate(aggregateInput))
        Some(Html(Messages("quick_calc.result.sidebar.pay_scottish_income_tax"))) else None,
      if(!aggregateConditions.isTaxCodeDefined(aggregateInput))
        Some(Html(Messages("quick_calc.result.sidebar.personal_allowance"))) else None,
      if(!aggregateConditions.isOverStatePensionAge(aggregateInput))
        Some(Html(Messages("quick_calc.result.sidebar.not_over_state_pension_age_a")
          + link("https://www.gov.uk/national-insurance-rates-letters/category-letters",
          Messages("quick_calc.result.sidebar.not_over_state_pension_age_b")))) else None,
      if (aggregateConditions.isOverStatePensionAge(aggregateInput))
        Some(Html(Messages("quick_calc.result.sidebar.over_state_pension_age"))) else None
    )
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
          Ok(resultView(TaxResult.taxCalculation(aggregate, defaultTaxCodeProvider), defaultTaxCodeProvider.startOfCurrentTaxYear, isScottish, salaryCheck(aggregate), getCurrentTaxYear,sideBarBullets(aggregate)))
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
