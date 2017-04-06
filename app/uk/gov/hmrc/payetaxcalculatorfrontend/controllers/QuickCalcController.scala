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

import play.api.data.Form
import play.api.http.DefaultHttpFilters
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.ActionWithSessionId
import uk.gov.hmrc.payetaxcalculatorfrontend.views.html.quickcalc._
import play.api.mvc._
import play.filters.csrf.CSRFFilter
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel._
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.TaxResult.omitScotland
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

@Singleton
class QuickCalcController @Inject()(override val messagesApi: MessagesApi, cache: QuickCalcCache)(csrfFilter: CSRFFilter)
                                    extends DefaultHttpFilters(csrfFilter) with FrontendController with I18nSupport {

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  def redirectToSalaryForm() = ActionWithSessionId { implicit request =>
    Redirect(routes.QuickCalcController.showSalaryForm())
  }

  private def tokenAction[T](furtherAction: Request[T] => Future[Result])(implicit bodyParser: BodyParser[T]): Action[T] =
    Action.async(bodyParser) { implicit request =>
      request.session.get("csrfToken").map(token => {
        furtherAction(request)
      })
        .getOrElse(Future.successful(Redirect(routes.QuickCalcController.showSalaryForm())))
    }

  def summary(): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        if (aggregate.allQuestionsAnswered) Ok(you_have_told_us(aggregate.youHaveToldUsItems))
        else redirectToNotYetDonePage(aggregate)
      case None => Redirect(routes.QuickCalcController.showSalaryForm())
    }
  }

  def showResult(): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        if (aggregate.allQuestionsAnswered) {
          val date = UserTaxCode.taxConfig(aggregate.savedTaxCode.get.taxCode.get)
          Ok(result(aggregate, omitScotland(date.taxYear), "", print = false))
        }
        else redirectToNotYetDonePage(aggregate)
      case None => Redirect(routes.QuickCalcController.showSalaryForm())
    }
  }

  def showPrint(): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        if (aggregate.allQuestionsAnswered) {
          val date = UserTaxCode.taxConfig(aggregate.savedTaxCode.get.taxCode.get)
          Ok(result(aggregate, date.taxYear, "open", print = true))
        }
        else redirectToNotYetDonePage(aggregate)
      case None => Redirect(routes.QuickCalcController.showSalaryForm())
    }
  }

  def showSalaryForm(): Action[AnyContent] = tokenAction[AnyContent] { implicit request =>
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        val form = {
          aggregate.savedSalary.map(s => Salary.salaryBaseForm.fill(s)).getOrElse(Salary.salaryBaseForm)
        }
        Ok(salary(form))
      case None =>
        Ok(salary(Salary.salaryBaseForm))
    }
  }

  def submitSalaryAmount(): Action[AnyContent] = tokenAction { implicit request =>
    val url = request.uri
    Salary.salaryBaseForm.bindFromRequest().fold(
      formWithErrors => Future(BadRequest(salary(formWithErrors))),

      salaryAmount => {
        val updatedAggregate = cache.fetchAndGetEntry()
                      .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
                        .map(_.copy(savedSalary = Some(salaryAmount), savedPeriod = None))

        updatedAggregate.flatMap(agg => cache.save(agg).map( _ => {
            salaryAmount.period match {
              case "a day" => Redirect(routes.QuickCalcController.showDaysAWeek(Salary.salaryInPence(salaryAmount.amount), url))
              case "an hour" => Redirect(routes.QuickCalcController.showHoursAWeek(Salary.salaryInPence(salaryAmount.amount), url))
              case _ => nextPageOrSummaryIfAllQuestionsAnswered(agg){
                Redirect(routes.QuickCalcController.showStatePensionForm())
            }
          }
        }))
      }
    )
  }

  def showHoursAWeek(valueInPence: Int, url: String): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      _ => Ok(hours_a_week(valueInPence, Salary.salaryInHoursForm, url))
    }
  }

  def submitHoursAWeek(valueInPence: Int): Action[AnyContent] = tokenAction { implicit request =>
    val url = routes.QuickCalcController.showSalaryForm().url
    val value = BigDecimal(valueInPence) / 100
    Salary.salaryInHoursForm.bindFromRequest().fold(
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
              Redirect(routes.QuickCalcController.showStatePensionForm())
          ))
        })
      }
    )
  }

  def showDaysAWeek(valueInPence: Int, url: String): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      _ => Ok(days_a_week(valueInPence, Salary.salaryInDaysForm, url))
    }
  }

  def submitDaysAWeek(valueInPence: Int): Action[AnyContent] = tokenAction { implicit request =>
    val url = routes.QuickCalcController.showSalaryForm().url
    val value = BigDecimal(valueInPence) / 100
    Salary.salaryInDaysForm.bindFromRequest().fold(
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
                Redirect(routes.QuickCalcController.showStatePensionForm())))
        }}
      }
    )
  }

  def showStatePensionForm(): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        val form = aggregate.savedIsOverStatePensionAge
          .map(OverStatePensionAge.form.fill)
          .getOrElse(OverStatePensionAge.form)
        Ok(state_pension(form, aggregate.youHaveToldUsItems))
      case None =>
        Ok(state_pension(OverStatePensionAge.form, Nil))
    }
  }

  def submitStatePensionForm(): Action[AnyContent] = tokenAction { implicit request =>
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
            Redirect(routes.QuickCalcController.showTaxCodeForm()) } }
        case None => cache.save(QuickCalcAggregateInput.newInstance.copy(savedIsOverStatePensionAge = Some(userAge))).map {
          _ => Redirect(routes.QuickCalcController.showTaxCodeForm())
        }
      }
    )
  }

  def showTaxCodeForm(): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        val form = aggregate.savedTaxCode.map(UserTaxCode.form.fill).getOrElse(UserTaxCode.form)
        Ok(tax_code(form, aggregate.youHaveToldUsItems))
      case None =>
        Ok(tax_code(UserTaxCode.form, Nil))
    }
  }
  // better design might obsoletes this need
  private def recoverTheUserState(formWithErrors: Form[UserTaxCode])(implicit messages: Messages) = {
    if (formWithErrors.errors.flatMap(_.messages).exists(er =>
      (er == messages(UserTaxCode.WRONG_TAX_CODE_SUFFIX_KEY)) ||
        (er == messages(UserTaxCode.WRONG_TAX_CODE_KEY))))
      formWithErrors.fill(UserTaxCode(gaveUsTaxCode = true, None))
    else formWithErrors
  }

  def submitTaxCodeForm(): Action[AnyContent] = tokenAction { implicit request =>
    UserTaxCode.form.bindFromRequest().fold(
      formWithErrors =>
        cache.fetchAndGetEntry().map {
          case Some(aggregate) =>
            val updatedForm: Form[UserTaxCode] = recoverTheUserState(formWithErrors)
            BadRequest(tax_code(updatedForm, aggregate.youHaveToldUsItems))
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
            nextPageOrSummaryIfAllQuestionsAnswered(agg) {Redirect(routes.QuickCalcController.summary())}
          }
          else Redirect(routes.QuickCalcController.showScottishRateForm())
        ))
      }
    )
  }

  def showScottishRateForm(): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().map {
      case Some(aggregate) =>
        val form = aggregate.savedScottishRate.map(ScottishRate.form.fill).getOrElse(ScottishRate.form)
        Ok(scottish_income_tax_rate(form, aggregate.youHaveToldUsItems))
      case None =>
        Ok(scottish_income_tax_rate(ScottishRate.form, Nil))
    }
  }

  def submitScottishRateForm(): Action[AnyContent] = tokenAction { implicit request =>
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
          .map( _ => Redirect(routes.QuickCalcController.summary()) )
      }
    )
  }

  def restartQuickCalc(): Action[AnyContent] = tokenAction { implicit request =>
    cache.fetchAndGetEntry().flatMap {
      case Some(aggregate) =>
        val updatedAggregate = aggregate.copy(None, None, None, None, None)
        cache.save(updatedAggregate).map { _ => Redirect(routes.QuickCalcController.showSalaryForm()) }
      case None =>
        Future.successful(Redirect(routes.QuickCalcController.showSalaryForm()))
    }
  }

  private def nextPageOrSummaryIfAllQuestionsAnswered(aggregate: QuickCalcAggregateInput)
                                                     (next: Result)
                                                     (implicit request: Request[_]): Result = {
    if (aggregate.allQuestionsAnswered) Redirect(routes.QuickCalcController.summary())
    else next
  }

  private def redirectToNotYetDonePage(aggregate: QuickCalcAggregateInput): Result = {
    if (aggregate.savedTaxCode.isEmpty) Redirect(routes.QuickCalcController.showTaxCodeForm())
    else if (aggregate.savedSalary.isEmpty) Redirect(routes.QuickCalcController.showSalaryForm())
    else Redirect(routes.QuickCalcController.showStatePensionForm())
  }
}
