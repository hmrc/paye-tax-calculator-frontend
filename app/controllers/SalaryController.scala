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
import forms.SalaryFormProvider
import javax.inject.{Inject, Singleton}
import models.Salary
import models.Salary.salaryInPence
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import services.{Navigator, QuickCalcCache, SalaryService}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.ActionWithSessionId
import views.html.pages.SalaryView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SalaryController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  salaryService:                 SalaryService,
  navigator:                     Navigator,
  salaryView:                    SalaryView,
  salaryFormProvider:            SalaryFormProvider
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[Salary] = salaryFormProvider()

  def showSalaryForm(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        val filledForm = aggregate.savedSalary.map(s => form.fill(s)).getOrElse(form)
        Ok(salaryView(filledForm))
      case None =>
        Ok(salaryView(form))
    }
  }

  def submitSalaryAmount(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    val url = request.uri
    val day:  String = Messages("quick_calc.salary.daily.label")
    val hour: String = Messages("quick_calc.salary.hourly.label")
    form
      .bindFromRequest()
      .fold(
        hasErrors = formWithErrors => {
          Future(BadRequest(salaryView(formWithErrors)))
        },
        success = salaryAmount => {
          val updatedAggregate = salaryService.updateSalaryAmount(cache, salaryAmount, url)

          updatedAggregate.flatMap(agg =>
            cache
              .save(agg)
              .map {
                _ =>
                  salaryAmount.period match {
                    case `day` =>
                      if (agg.savedPeriod.map(_.period).contains(day))
                        Redirect(navigator.tryGetShowStatePension(agg))
                      else
                        Redirect(routes.DaysPerWeekController.showDaysAWeek(salaryInPence(salaryAmount.amount), url))
                    case `hour` =>
                      if (agg.savedPeriod.map(_.period).contains(hour))
                        Redirect(navigator.tryGetShowStatePension(agg))
                      else
                        Redirect(routes.HoursPerWeekController.showHoursAWeek(salaryInPence(salaryAmount.amount), url))
                    case _ => Redirect(navigator.tryGetShowStatePension(agg))
                  }
              }
          )
        }
      )
  }

}
