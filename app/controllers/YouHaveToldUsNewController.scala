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
import play.api.i18n.{I18nSupport, MessagesApi}
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

  private def taxCodeCheck(aggregateInput: QuickCalcAggregateInput): Map[String, String] = {
    val condition1: Boolean = aggregateInput.savedTaxCode.flatMap(_.taxCode.map(_.contains("S"))).getOrElse(false)
    val condition2: Boolean = aggregateInput.savedScottishRate.exists(!_.payScottishRate)
    val condition3: Boolean = aggregateInput.savedScottishRate.exists(_.payScottishRate)
    val condition4: Boolean = !condition1
    val counter = 0
    val taxCodeWarning: List[(String, String)] =
      if (aggregateInput.savedSalary.exists(_.amount > 100000)
        && !aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultUkTaxCode)
        || !aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultScottishTaxCode)))) {
        List("about_tax_code" -> "This tax code is not usually for those on more than £100,000 a year.")
      }
      else if (aggregateInput.savedSalary.exists(_.amount > 100000)
      && aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultUkTaxCode)
      || aggregateInput.savedTaxCode.flatMap(_.taxCode).exists(_.equals(defaultTaxCodeProvider.defaultScottishTaxCode)))) {
      List("about_tax_code" -> "This tax code is not usually for those on more than £100,000 a year.")
    } else if (condition1 && condition2) {
      List("about_tax_code" -> "You've used a Scottish tax code so we will apply Scottish Income Tax rates.")
    } else {
      List.empty
    }
    val scotRate: List[(String, String)] = if (condition3 && condition4) List("scottish_rate" -> "You said you pay Scottish Income Tax so we will apply Scottish Income Tax rates.") else List.empty
    val finalList = taxCodeWarning ++ scotRate
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
