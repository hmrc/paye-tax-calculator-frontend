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

import javax.inject.{Inject, Singleton}
import models.QuickCalcAggregateInput
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, DefaultTaxCodeProvider}
import views.html.pages.{YouHaveToldUsNewView, YouHaveToldUsView}

import scala.concurrent.ExecutionContext

@Singleton
class YouHaveToldUsNewController @Inject() (
                                          override val messagesApi:      MessagesApi,
                                          cache:                         QuickCalcCache,
                                          val controllerComponents:      MessagesControllerComponents,
                                          navigator:                     Navigator,
                                          yourHaveToldUsView:            YouHaveToldUsNewView,
                                          defaultTaxCodeProvider: DefaultTaxCodeProvider
                                        )(implicit val appConfig:        AppConfig,
                                          implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val taxCodeLabel: String = "about_tax_code"
  val scottishRateLabel: String = "scottish_rate"

  def summary(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        aggregate =>
          if (aggregate.allQuestionsAnswered)
            Ok(yourHaveToldUsView(aggregate.youHaveToldUsItems,aggregate.additionalQuestionItems,  taxCodeCheck(aggregate), isTaxCodeDefined(aggregate)))
          else
            Redirect(navigator.redirectToNotYetDonePage(aggregate))
    )

  def isTaxCodeDefined(aggregateInput: QuickCalcAggregateInput): Boolean = {
    aggregateInput.savedTaxCode.flatMap(_.taxCode).isDefined
  }

  def taxCodeContainsS(aggregateInput: QuickCalcAggregateInput) : Boolean = {
    aggregateInput.savedTaxCode.flatMap(_.taxCode.map(_.contains("S"))).getOrElse(false)
  }

  def payScottishRate(aggregateInput: QuickCalcAggregateInput) : Boolean = {
    aggregateInput.savedScottishRate.exists(_.payScottishRate)
  }

  def salaryOverHundredThousand(aggregateInput: QuickCalcAggregateInput) : Boolean = {
    aggregateInput.savedSalary.exists(_.amount > 100000)
  }
  def isUkOrScottishTaxCode(aggregateInput: QuickCalcAggregateInput) : Boolean = {
    aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultUkTaxCode)
      || aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultScottishTaxCode)))
  }

  def taxCodeWarnings(aggregateInput: QuickCalcAggregateInput)(implicit messages: Messages) : List[(String, String)] = {
    if (salaryOverHundredThousand(aggregateInput) && isUkOrScottishTaxCode(aggregateInput)) {
      List(taxCodeLabel -> Messages("quick_calc.tax_code.over_hundred_thousand.warning"))
    } else if (taxCodeContainsS(aggregateInput) && !payScottishRate(aggregateInput)) {
      List(taxCodeLabel -> Messages("quick_calc.tax_code.scottish_rate.warning"))
    } else {
      List.empty
    }
  }

  def scottishRateWarnings(aggregateInput: QuickCalcAggregateInput)(implicit messages: Messages): List[(String,String)] = {
    if (payScottishRate(aggregateInput) && !taxCodeContainsS(aggregateInput))
      List(scottishRateLabel -> Messages("quick_calc.scottish_rate.payScottishRate.warning")) else List.empty
  }

  private def taxCodeCheck(aggregateInput: QuickCalcAggregateInput)(implicit messages: Messages): Map[String, String] = {
    val finalList = taxCodeWarnings(aggregateInput) ++ scottishRateWarnings(aggregateInput)
    finalList.toMap
  }

  private def salaryRequired[T](
                                 cache:         QuickCalcCache,
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
