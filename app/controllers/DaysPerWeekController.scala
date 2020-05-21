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
import forms.SalaryInDaysFormProvider
import javax.inject.{Inject, Singleton}
import models.{Days, Hours, PayPeriodDetail, QuickCalcAggregateInput, Salary}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, ControllerComponents, MessagesControllerComponents, Request, Result}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.controller.{BackendBaseController, FrontendBaseController}
import utils.ActionWithSessionId
import views.html.pages.DaysAWeekView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DaysPerWeekController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents: MessagesControllerComponents,
  navigator:                     Navigator,
  daysPerWeekView:                DaysAWeekView,
  salaryInDaysFormProvider: SalaryInDaysFormProvider,
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[Days] = salaryInDaysFormProvider()

  def submitDaysAWeek(valueInPence: Int): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    val url   = routes.SalaryController.showSalaryForm().url
    val value = BigDecimal(valueInPence)
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          cache.fetchAndGetEntry().map { _ =>
            BadRequest(daysPerWeekView( formWithErrors, valueInPence,url))
          },
        days => {
          val updatedAggregate = cache
            .fetchAndGetEntry()
            .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
            .map(
              _.copy(
                savedSalary = Some(Salary(value, Messages("quick_calc.salary.daily.label"), Some(days.howManyAWeek))),
                savedPeriod =
                  Some(PayPeriodDetail(valueInPence, days.howManyAWeek, Messages("quick_calc.salary.daily.label"), url))
              )
            )

          updatedAggregate.flatMap { agg =>
            cache
              .save(agg)
              .map(_ =>
                Redirect(
                  navigator.nextPageOrSummaryIfAllQuestionsAnswered(agg)(
                    routes.StatePensionController.showStatePensionForm()
                  )
                )
              )
          }
        }
      )
  }

  def showDaysAWeek(
                     valueInPence: Int,
                     url:          String
                   ): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    salaryRequired(cache,Ok(daysPerWeekView(form, valueInPence, url)))
  }


  private def salaryRequired[T](
                                 cache: QuickCalcCache,
                                 resultIfPresent: Result
                               )(implicit hc: HeaderCarrier): Future[Result] = {

    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        if (aggregate.savedSalary.isDefined)
          resultIfPresent
        else
          Redirect(routes.SalaryController.showSalaryForm())
      case None =>
        Redirect(routes.SalaryController.showSalaryForm())
    }
  }

}
