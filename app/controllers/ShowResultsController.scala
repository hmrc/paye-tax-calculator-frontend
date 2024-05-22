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
import forms.TaxResult.moneyFormatter

import javax.inject.{Inject, Singleton}
import models.QuickCalcAggregateInput
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import play.twirl.api.Html
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.calculator.exception.InvalidPensionException
import uk.gov.hmrc.calculator.model.CalculatorResponse
import uk.gov.hmrc.calculator.utils.clarification.Clarification
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.GetCurrentTaxYear.getCurrentTaxYear
import utils.{ActionWithSessionId, AggregateConditionsUtil, DefaultTaxCodeProvider, SalaryRequired}
import views.html.components.linkNewTab
import views.html.pages.ResultView

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext

@Singleton
class ShowResultsController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  resultView:                    ResultView,
  defaultTaxCodeProvider:        DefaultTaxCodeProvider,
  aggregateConditions:           AggregateConditionsUtil,
  linkInNewTab:                  linkNewTab
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId with SalaryRequired{

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  private def over100KDisclaimerCheck(aggregateInput: QuickCalcAggregateInput): Boolean = {
    val listOfClarifications = getClarifications(aggregateInput, defaultTaxCodeProvider)
      listOfClarifications.contains(Clarification.INCOME_OVER_100K)
  }

  private def getTaxCalculation(
    aggregateInput:         QuickCalcAggregateInput,
    defaultTaxCodeProvider: DefaultTaxCodeProvider
  ): CalculatorResponse =
    TaxResult.taxCalculation(aggregateInput, defaultTaxCodeProvider)

  private def getClarifications(
    aggregateInput:         QuickCalcAggregateInput,
    defaultTaxCodeProvider: DefaultTaxCodeProvider
  ): List[Clarification] =
    getTaxCalculation(aggregateInput, defaultTaxCodeProvider).getListOfClarification.asScala.toList

  def sideBarBullets(aggregateInput: QuickCalcAggregateInput)(implicit messages: Messages): Seq[Option[Html]] = {
    val listOfClarifications = getClarifications(aggregateInput, defaultTaxCodeProvider)

    val keyValuePair: Map[Clarification, Option[Html]] = Map(
      Clarification.NO_TAX_CODE_SUPPLIED -> Some(
        Html(
          Messages("quick_calc.result.sidebar.personal_allowance",
                   moneyFormatter(getTaxCalculation(aggregateInput, defaultTaxCodeProvider).getYearly.getTaxFree))
        )
      ),
      Clarification.HAVE_STATE_PENSION -> Some(Html(Messages("quick_calc.result.sidebar.over_state_pension_age"))),
      Clarification.HAVE_NO_STATE_PENSION -> Some(
                Html(
                  Messages("quick_calc.result.sidebar.not_over_state_pension_age_a")
                  + linkInNewTab("https://www.gov.uk/national-insurance-rates-letters/category-letters",
                                 Messages("quick_calc.result.sidebar.not_over_state_pension_age_b"))
                )
              ),
      Clarification.SCOTTISH_INCOME_APPLIED -> Some(
        Html(Messages("quick_calc.result.sidebar.appliedScottishIncomeTaxRates"))
      ),
      Clarification.SCOTTISH_CODE_BUT_OTHER_RATE -> Some(Html(Messages("quick_calc.result.sidebar.scottish_tax_code"))),
      Clarification.NON_SCOTTISH_CODE_BUT_SCOTTISH_RATE -> Some(
        Html(Messages("quick_calc.result.sidebar.pay_scottish_income_tax"))
      ),
      Clarification.INCOME_OVER_100K_WITH_TAPERING -> Some(
        Html(
          Messages("quick_calc.result.sidebar.youHaveReducedPersonal_allowance_a")
          + linkInNewTab("https://www.gov.uk/income-tax-rates/income-over-100000",
                         Messages("quick_calc.result.sidebar.youHaveReducedPersonal_allowance_b")) + Messages(
            "quick_calc.result.sidebar.youHaveReducedPersonal_allowance_c"
          )
        )
      ),
      Clarification.K_CODE -> Some(
        Html(
          Messages("quick_calc.result.sidebar.kcode_a")
          + linkInNewTab(
            "https://www.gov.uk/income-tax/taxfree-and-taxable-state-benefits",
            Messages("quick_calc.result.sidebar.kcode_b")
          ) + Messages("quick_calc.result.sidebar.kcode_c") + linkInNewTab(
            "https://www.gov.uk/tax-company-benefits",
            Messages("quick_calc.result.sidebar.kcode_d")
          )
        )
      )
    )

    val seqOfBullet: ListBuffer[Option[Html]] = ListBuffer(Some(Html(Messages("quick_calc.result.sidebar.one_job"))))

    listOfClarifications.foreach { clarification =>
      val text = keyValuePair.getOrElse(clarification, default = None)
      seqOfBullet += text
    }

    seqOfBullet.toSeq
  }

  def showResult(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        aggregate =>
          if (aggregate.allQuestionsAnswered) {
            val isScottish = if (aggregate.savedTaxCode.exists(_.taxCode.exists(_.contains("S")))) {
              true
            } else if (aggregate.savedScottishRate.exists(_.payScottishRate)) {
              true
            } else {
              false
            }
            try {
              Ok(
                resultView(
                  TaxResult.taxCalculation(aggregate, defaultTaxCodeProvider),
                  defaultTaxCodeProvider.startOfCurrentTaxYear,
                  isScottish,
                  over100KDisclaimerCheck(aggregate),
                  getCurrentTaxYear,
                  sideBarBullets(aggregate),
                  aggregateConditions.isPensionContributionsDefined(aggregate)
                )
              )
            } catch {
              case _: InvalidPensionException =>
                Redirect(controllers.routes.YouHaveToldUsNewController.summary)
            }
          } else Redirect(navigator.redirectToNotYetDonePage(aggregate))
    )

}
