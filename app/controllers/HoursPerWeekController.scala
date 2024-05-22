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
import forms.{SalaryInHoursFormProvider, TaxResult}

import javax.inject.{Inject, Singleton}
import models.{Hours, PayPeriodDetail, QuickCalcAggregateInput, Salary}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, BigDecimalFormatter, SalaryRequired}
import views.html.pages.HoursAWeekView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HoursPerWeekController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  hoursAWeekView:                HoursAWeekView,
  salaryInHoursFormProvider:     SalaryInHoursFormProvider
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId with SalaryRequired{

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[Hours] = salaryInHoursFormProvider()

  def submitHoursAWeek(valueInPence: Int): Action[AnyContent] = validateAcceptWithSessionId().async { implicit request =>
    implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

    val url   = routes.SalaryController.showSalaryForm.url
    val value = BigDecimal(valueInPence / 100.0)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future(BadRequest(hoursAWeekView(formWithErrors, value))),
        hours => {
          val currentYearlyAmount: BigDecimal = TaxResult.convertWagesToYearly(value, Messages("quick_calc.salary.hourly.label"), Some(hours.howManyAWeek))
          val currentMonthlyAmount: BigDecimal = TaxResult.convertWagesToMonthly(currentYearlyAmount)
          val updatedAggregate = cache
            .fetchAndGetEntry()
            .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
            .map(oldAggregate =>
              oldAggregate.copy(
                savedSalary = Some(
                  Salary(value,
                         Some(currentYearlyAmount),
                         oldAggregate.savedSalary.flatMap(_.amountYearly),
                         Messages("quick_calc.salary.hourly.label"),
                         Some(hours.howManyAWeek),
                         Some(currentMonthlyAmount))
                ),
                savedPeriod = Some(
                  PayPeriodDetail(value, hours.howManyAWeek, Messages("quick_calc.salary.hourly.label"), url)
                )
              )
            )
          updatedAggregate.flatMap { agg =>
            cache
              .save(agg)
              .map(_ =>
                Redirect(
                  navigator.nextPageOrSummaryIfAllQuestionsAnswered(agg)(
                    routes.StatePensionController.showStatePensionForm
                  )()
                )
              )
          }
        }
      )
  }

  def showHoursAWeek(valueInPence: Int): Action[AnyContent] =
    salaryRequired(
      cache, { implicit request => agg =>
        val filledForm = agg.savedPeriod
          .map(s => form.fill(Hours(s.amount, BigDecimalFormatter.stripZeros(s.howManyAWeek.bigDecimal))))
          .getOrElse(form)

        Ok(hoursAWeekView(filledForm, BigDecimal(valueInPence / 100.0)))

      }
    )

}
