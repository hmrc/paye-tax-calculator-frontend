/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.http.Status
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.TaxResult.omitScotland
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel._
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.Salary._
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.ActionWithSessionId
import uk.gov.hmrc.payetaxcalculatorfrontend.views.html.quickcalc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.routes.{QuickCalcController => qc}

import scala.concurrent.Future

@Singleton
class QuickCalcController @Inject()(override val messagesApi: MessagesApi, cache: QuickCalcCache)
                                    extends FrontendController with I18nSupport {

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  def redirectToSalaryForm(): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    Future.successful(Redirect(qc.showSalaryForm()))
  }

  private def salaryRequired[T](furtherAction: Request[AnyContent] => QuickCalcAggregateInput => Result): Action[AnyContent] =
    ActionWithSessionId.async { implicit request =>
        cache.fetchAndGetEntry().map {
          case Some(aggregate) =>
              if (aggregate.savedSalary.isDefined)
                furtherAction(request)(aggregate)
              else
                Redirect(qc.showSalaryForm())
          case None =>
            Redirect(qc.showSalaryForm())
      }
    }

  def showSalaryForm(): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    Logger.info("app started")
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        val form = {
          aggregate.savedSalary.map(s => salaryBaseForm.fill(s)).getOrElse(salaryBaseForm)
        }
        Ok(salary(form))
      case None =>
        Ok(salary(salaryBaseForm))
    }
  }

  def summary(): Action[AnyContent] = salaryRequired( implicit request => aggregate =>
    if (aggregate.allQuestionsAnswered) Ok(you_have_told_us(aggregate.youHaveToldUsItems))
    else redirectToNotYetDonePage(aggregate)
  )

  def showResult(): Action[AnyContent] = salaryRequired( implicit request => aggregate =>
    if (aggregate.allQuestionsAnswered) {
      val date = UserTaxCode.taxConfig(aggregate.savedTaxCode.get.taxCode.get)
      Ok(result(aggregate, omitScotland(date.taxYear)))
    }
    else redirectToNotYetDonePage(aggregate)
  )

  def submitSalaryAmount(): Action[AnyContent] = Action.async { implicit request =>
    val url = request.uri
    val day: String = Messages("quick_calc.salary.daily.label")
    val hour: String = Messages("quick_calc.salary.hourly.label")
    salaryBaseForm.bindFromRequest().fold(
      formWithErrors => Future(BadRequest(salary(formWithErrors))),
      salaryAmount => {
        val updatedAggregate = updateSalaryAmount(salaryAmount, url)

        updatedAggregate.flatMap(agg => cache.save(agg).map( _ => {
          salaryAmount.period match {
            case `day` =>
              if(agg.savedPeriod.map(_.period).contains(day)) tryGetShowStatePension(agg)
              else Redirect(qc.showDaysAWeek(salaryInPence(salaryAmount.amount), url))
            case `hour` =>
              if(agg.savedPeriod.map(_.period).contains(hour)) tryGetShowStatePension(agg)
              else Redirect(qc.showHoursAWeek(salaryInPence(salaryAmount.amount), url))
            case _ => tryGetShowStatePension(agg)
          }
        }
        ))
      }
    )
  }

  private def tryGetShowStatePension(agg: QuickCalcAggregateInput)(implicit request: Request[AnyContent]) = {
    nextPageOrSummaryIfAllQuestionsAnswered(agg) {
      Redirect(qc.showStatePensionForm())
    }
  }

  private def updateSalaryAmount(salaryAmount: Salary, url:String)(implicit request: Request[AnyContent]) = {
    cache.fetchAndGetEntry()
      .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
      .map(oldAggregate => {
        val newAggregate = oldAggregate.copy(savedSalary = Some(salaryAmount))
        (newAggregate.savedSalary, newAggregate.savedPeriod) match {
          case (Some(salary), Some(detail)) =>
            if (salary.period == oldAggregate.savedSalary.map(_.period).getOrElse("")) {
              newAggregate.copy(
                savedSalary = Some(Salary(salaryAmount.amount, salary.period, oldAggregate.savedSalary.get.howManyAWeek)),
                savedPeriod = Some(Detail((salaryAmount.amount*100).toInt, detail.howManyAWeek, detail.period, url))
              ) }
            else newAggregate.copy(savedPeriod = None)
          case _ => newAggregate.copy(savedPeriod = None)
        }
      }
      )
  }

  def showHoursAWeek(valueInPence: Int, url: String): Action[AnyContent] = {
    salaryRequired(showHoursAWeekTestable(valueInPence, url))
  }

  private[controllers] def showHoursAWeekTestable(valueInPence: Int, url: String): ShowForm = {
    implicit request => agg =>
      Ok(hours_a_week(valueInPence, salaryInHoursForm, url))
  }

  def submitHoursAWeek(valueInPence: Int): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    val url = qc.showSalaryForm().url
    val value = BigDecimal(valueInPence) / 100
    salaryInHoursForm.bindFromRequest().fold(
      formWithErrors => cache.fetchAndGetEntry().map {
        _ => BadRequest(hours_a_week(valueInPence, formWithErrors, url))
      },
      hours => {
        val updatedAggregate = cache.fetchAndGetEntry()
          .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
            .map(_.copy(savedSalary = Some(Salary(value, Messages("quick_calc.salary.hourly.label"), Some(hours.howManyAWeek))),
                        savedPeriod = Some(Detail(valueInPence, hours.howManyAWeek, Messages("quick_calc.salary.hourly.label"), url))))

        updatedAggregate.flatMap( agg => {
          cache.save(agg).map( _ =>
            nextPageOrSummaryIfAllQuestionsAnswered(agg)(
              Redirect(qc.showStatePensionForm())
          ))
        })
      }
    )
  }

  def showDaysAWeek(valueInPence: Int, url: String): Action[AnyContent] =
    salaryRequired(showDaysAWeekTestable(valueInPence, url))

  private[controllers] def showDaysAWeekTestable(valueInPence: Int, url: String): ShowForm =
    implicit request => agg => Ok(days_a_week(valueInPence, salaryInDaysForm, url))


  def submitDaysAWeek(valueInPence: Int): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    val url = qc.showSalaryForm().url
    val value = BigDecimal(valueInPence) / 100
    salaryInDaysForm.bindFromRequest().fold(
      formWithErrors => cache.fetchAndGetEntry().map {
        _ => BadRequest(days_a_week(valueInPence, formWithErrors, url))
      },
      days => {
        val updatedAggregate = cache.fetchAndGetEntry()
          .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
            .map(_.copy(savedSalary = Some(Salary(value, Messages("quick_calc.salary.daily.label"), Some(days.howManyAWeek))),
                        savedPeriod = Some(Detail(valueInPence, days.howManyAWeek,Messages("quick_calc.salary.daily.label"), url))))

        updatedAggregate.flatMap{ agg => {
          cache.save(agg)
            .map( _ =>
              nextPageOrSummaryIfAllQuestionsAnswered(agg) (
                Redirect(qc.showStatePensionForm())))
        }}
      }
    )
  }

  def showStatePensionForm(): Action[AnyContent] = salaryRequired(showStatePensionFormTestable)

  private[controllers] def showStatePensionFormTestable: ShowForm = {
    implicit request => agg => {
      val form = agg.savedIsOverStatePensionAge
        .map(OverStatePensionAge.form.fill)
        .getOrElse(OverStatePensionAge.form)
      Ok(state_pension(form, agg.youHaveToldUsItems))
    }
  }

  def submitStatePensionForm(): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    OverStatePensionAge.form.bindFromRequest().fold(
      formWithErrors => cache.fetchAndGetEntry().map {
        case Some(aggregate) =>
          BadRequest(state_pension(formWithErrors, aggregate.youHaveToldUsItems))
        case None =>
          BadRequest(state_pension(formWithErrors, Nil))
      },
      userAge => cache.fetchAndGetEntry().flatMap {
        case Some(aggregate) =>
          val updatedAggregate = aggregate.copy(savedIsOverStatePensionAge = Some(userAge))
          cache.save(updatedAggregate).map { _ =>
            nextPageOrSummaryIfAllQuestionsAnswered(updatedAggregate) {
              Redirect(qc.showTaxCodeForm())
            }
          }
        case None => cache.save(QuickCalcAggregateInput.newInstance.copy(savedIsOverStatePensionAge = Some(userAge))).map {
          _ => Redirect(qc.showTaxCodeForm())
        }
      }
    )
  }

  def showTaxCodeForm(): Action[AnyContent] = salaryRequired(showTacCodeFormTestable)

  private[controllers] val showTacCodeFormTestable: ShowForm =
    implicit request => aggregate => {
      val form = aggregate.savedTaxCode.map(UserTaxCode.form.fill).getOrElse(UserTaxCode.form)
      Ok(tax_code(form, aggregate.youHaveToldUsItems))
    }

  def submitTaxCodeForm(): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    UserTaxCode.form.bindFromRequest().fold(
      formWithErrors =>
        cache.fetchAndGetEntry().map {
          case Some(aggregate) =>
            BadRequest(tax_code(formWithErrors, aggregate.youHaveToldUsItems))
          case None => BadRequest(tax_code(formWithErrors, Nil))
        },
      newTaxCode => {
        val updatedAggregate = cache.fetchAndGetEntry()
          .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
          .map(agg =>
            if (newTaxCode.gaveUsTaxCode) agg.copy(savedTaxCode = Some(newTaxCode), savedScottishRate = None)
            else agg.copy(savedTaxCode = Some(UserTaxCode(gaveUsTaxCode = false, Some(UserTaxCode.DEFAULT_TAX_CODE))))
          )

        updatedAggregate.flatMap(agg => cache.save(agg).map(_ =>
          if (newTaxCode.gaveUsTaxCode) {
            nextPageOrSummaryIfAllQuestionsAnswered(agg) {
              Redirect(qc.summary())
            }
          }
          else Redirect(qc.showScottishRateForm())
        ))
      }
    )
  }

  def showScottishRateForm(): Action[AnyContent] = salaryRequired( implicit request => aggregate => {
    val form = aggregate.savedScottishRate.map(ScottishRate.form.fill).getOrElse(ScottishRate.form)
    Ok(scottish_income_tax_rate(form, aggregate.youHaveToldUsItems))
  })

  def submitScottishRateForm(): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    ScottishRate.form.bindFromRequest().fold(
      formWithErrors => {
        cache.fetchAndGetEntry().map {
          case Some(aggregate) => aggregate.youHaveToldUsItems
          case None => Nil
        }.map( itemList =>
          BadRequest(scottish_income_tax_rate(formWithErrors, itemList))
        )
      },
      scottish => {
        val taxCode = if (scottish.value) UserTaxCode.DEFAULT_SCOTTISH_TAC_CODE else UserTaxCode.DEFAULT_TAX_CODE

        val updatedAggregate = cache.fetchAndGetEntry()
          .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
          .map(_.copy(
            savedTaxCode = Some(UserTaxCode(gaveUsTaxCode = false, Some(taxCode))),
            savedScottishRate = Some(ScottishRate(scottish.value)))
          )
        updatedAggregate
          .map(cache.save)
          .map( _ => Redirect(qc.summary()) )
      }
    )
  }

  def restartQuickCalc(): Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry().flatMap {
      case Some(aggregate) =>
        val updatedAggregate = aggregate.copy(None, None, None, None, None)
        cache.save(updatedAggregate).map { _ => Redirect(qc.showSalaryForm()) }
      case None =>
        Future.successful(Redirect(qc.showSalaryForm()))
    }
  }

  private def nextPageOrSummaryIfAllQuestionsAnswered(aggregate: QuickCalcAggregateInput)
                                                     (next: Result)
                                                     (implicit request: Request[_]): Result = {
    if (aggregate.allQuestionsAnswered) Redirect(qc.summary())
    else next
  }

  private def redirectToNotYetDonePage(aggregate: QuickCalcAggregateInput): Result = {
    if (aggregate.savedTaxCode.isEmpty) Redirect(qc.showTaxCodeForm())
    else if (aggregate.savedSalary.isEmpty) Redirect(qc.showSalaryForm())
    else Redirect(qc.showStatePensionForm())
  }
}
