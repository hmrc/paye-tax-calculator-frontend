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
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.calculator.model.ValidationError
import uk.gov.hmrc.calculator.utils.validation.TaxCodeValidator
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ActionWithSessionId, AggregateConditionsUtil, DefaultTaxCodeProvider, SalaryRequired}
import views.html.components.notification
import views.html.pages.YouHaveToldUsNewView
import scala.concurrent.ExecutionContext

@Singleton
class YouHaveToldUsNewController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  yourHaveToldUsView:            YouHaveToldUsNewView,
  aggregateConditionsUtil:       AggregateConditionsUtil
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId
    with SalaryRequired {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  private val taxCodeLabel: String = "about_tax_code"
  val SCOTTISH_RATE = "scottish_rate"

  def summary(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        aggregate =>
          if (aggregate.allQuestionsAnswered) {
            Ok(
              yourHaveToldUsView(
                aggregate.youHaveToldUsItems(),
                aggregate.additionalQuestionItems(),
                taxCodeCheck(aggregate),
                aggregateConditionsUtil.isTaxCodeDefined(aggregate),
                aggregateConditionsUtil.isPensionContributionsDefined(aggregate),
                aggregateConditionsUtil.givenPensionContributionPercentage(aggregate),
                aggregateConditionsUtil.givenStudentLoanContribution(aggregate),
                aggregateConditionsUtil.givenPostGradLoanContribution(aggregate),
                aggregateConditionsUtil.isPensionWarning(aggregate),
                aggregateConditionsUtil.roundedMonthlySalary(aggregate),
                aggregateConditionsUtil.isScottishRateDefined(aggregate)
              )
            )
          } else
            Redirect(navigator.redirectToNotYetDonePage(aggregate))
    )

  private def taxCodeWarnings(
    aggregateInput:    QuickCalcAggregateInput
  )(implicit messages: Messages
  ): List[(String, String)] = {
    val payScottishRate = aggregateInput.savedScottishRate.exists(_.payScottishRate.getOrElse(false))
    val taxCode         = aggregateInput.savedTaxCode.flatMap(_.taxCode).getOrElse("")
    val result = Option(TaxCodeValidator.INSTANCE.validateTaxCodeMatchingRate(taxCode, payScottishRate))
    result.map(_.getErrorType) match {
      case Some(ValidationError.ScottishCodeButOtherRate) =>
        List(taxCodeLabel -> Messages("quick_calc.tax_code.scottish_rate.warning"))
      case Some(ValidationError.NonScottishCodeButScottishRate) =>
        List(SCOTTISH_RATE -> Messages("quick_calc.scottish_rate.payScottishRate.warning"))
      case _ => List.empty
    }
  }

  private def taxCodeCheck(
    aggregateInput:    QuickCalcAggregateInput
  )(implicit messages: Messages
  ): Map[String, String] = {
    val finalList = taxCodeWarnings(aggregateInput)
    finalList.toMap
  }
}
