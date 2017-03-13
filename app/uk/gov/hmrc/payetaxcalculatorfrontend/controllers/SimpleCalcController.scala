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

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.ActionWithSessionId
import uk.gov.hmrc.payetaxcalculatorfrontend.views.html.simplecalc.{age, days_a_week, hours_a_week, salary}
import play.api.mvc._
import uk.gov.hmrc.payetaxcalculatorfrontend.simplemodel.Salary
import uk.gov.hmrc.payetaxcalculatorfrontend.services.SimpleCalcCache
import uk.gov.hmrc.payetaxcalculatorfrontend.simplemodel._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

@Singleton
class SimpleCalcController @Inject()(override val messagesApi: MessagesApi,
                                     cache: SimpleCalcCache) extends FrontendController with I18nSupport {

  def showSalaryForm() = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry.map {
      case Some(aggregate) =>
        val form = aggregate.salary.map(Salary.salaryBaseForm.fill).getOrElse(Salary.salaryBaseForm)
        Ok(salary(form, aggregate.youHaveToldUsItems))
      case None =>
        Ok(salary(Salary.salaryBaseForm, Nil))
    }
  }

  def submitSalaryAmount() = ActionWithSessionId.async { implicit request =>
    Salary.salaryBaseForm.bindFromRequest.fold(
      formWithErrors => cache.fetchAndGetEntry.map {
        case Some(aggregate) => BadRequest(salary(formWithErrors, aggregate.youHaveToldUsItems))
        case None => BadRequest(salary(formWithErrors, Nil))
      },
      salaryAmount => cache.fetchAndGetEntry.flatMap {
        case Some(aggregate) => {
          val updatedAggregate = aggregate.copy(salary = Some(salaryAmount))
          `salaryAmount`.period match {
            case "daily" =>
              cache.save(updatedAggregate).map { _ => Redirect(routes.SimpleCalcController.showDaysAWeek(Salary.salaryInPence(salaryAmount.value)))}
            case "hourly" =>
              cache.save(updatedAggregate).map { _ => Redirect(routes.SimpleCalcController.showHoursAWeek(Salary.salaryInPence(salaryAmount.value)))}
            case _ =>
              cache.save(updatedAggregate).map { _ => Redirect(routes.SimpleCalcController.showAgeForm())}
          }
        }
        case None => cache.save(SimpleCalcAggregateInput.newInstance.copy(salary = Some(salaryAmount))).map {
          _ => Redirect(routes.SimpleCalcController.showAgeForm())
        }
      }
    )
  }

  def showAgeForm() = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry.map {
      case Some(aggregate) =>
        val form = aggregate.isOverStatePensionAge.map(OverStatePensionAge.form.fill).getOrElse(OverStatePensionAge.form)
        Ok(age(form, aggregate.youHaveToldUsItems))
      case None =>
        Ok(age(OverStatePensionAge.form, Nil))
    }
  }

  def submitAgeForm() = ActionWithSessionId.async { implicit request =>
    OverStatePensionAge.form.bindFromRequest.fold(
      formWithErrors => cache.fetchAndGetEntry.map {
        case Some(aggregate) => BadRequest(age(formWithErrors, aggregate.youHaveToldUsItems))
        case None => BadRequest(age(formWithErrors, Nil))
      },
      userAge => cache.fetchAndGetEntry.flatMap {
        case Some(aggregate) =>
          val updatedAggregate = aggregate.copy(isOverStatePensionAge = Some(userAge))
          cache.save(updatedAggregate).map { _ => Redirect(routes.SimpleCalcController.showAgeForm())}
        case None => cache.save(SimpleCalcAggregateInput.newInstance.copy(isOverStatePensionAge = Some(userAge))).map {
          _ =>  Redirect(routes.SimpleCalcController.showAgeForm())
        }
      }
    )
  }

  def showHoursAWeek(valueInPence: Int) = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry.map {
      case Some(aggregate) =>
        Ok(hours_a_week(valueInPence, Salary.salaryInHoursForm, aggregate.youHaveToldUsItems))
      case None =>
        Ok(hours_a_week(valueInPence, Salary.salaryInHoursForm, Nil))
    }
  }

  def submitHoursAWeek(valueInPence: Int) = ActionWithSessionId.async { implicit request =>
    Salary.salaryInHoursForm.bindFromRequest.fold(
      formWithErrors => cache.fetchAndGetEntry.map {
        case Some(aggregate) => BadRequest(hours_a_week(valueInPence, formWithErrors, aggregate.youHaveToldUsItems))
        case None => BadRequest(hours_a_week(valueInPence, formWithErrors, Nil))
      },
      hours => cache.fetchAndGetEntry.flatMap {
        case Some(aggregate) =>
          val updatedAggregate = aggregate.copy(salary = Some(Salary(valueInPence, "hourly")))
          cache.save(updatedAggregate).map { _ => Redirect(routes.SimpleCalcController.showAgeForm())}
        case None => cache.save(SimpleCalcAggregateInput.newInstance.copy(salary = Some(Salary(valueInPence, "hourly")))).map {
          _ =>  Redirect(routes.SimpleCalcController.showAgeForm())
        }
      }
    )
  }

  def showDaysAWeek(valueInPence: Int) = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry.map {
      case Some(aggregate) =>
        Ok(days_a_week(valueInPence, Salary.salaryInDaysForm, aggregate.youHaveToldUsItems))
      case None =>
        Ok(days_a_week(valueInPence, Salary.salaryInDaysForm, Nil))
    }
  }

  def submitDaysAWeek(valueInPence: Int) = ActionWithSessionId.async { implicit request =>
    Salary.salaryInDaysForm.bindFromRequest.fold(
      formWithErrors => cache.fetchAndGetEntry.map {
        case Some(aggregate) => BadRequest(days_a_week(valueInPence, formWithErrors, aggregate.youHaveToldUsItems))
        case None => BadRequest(days_a_week(valueInPence, formWithErrors, Nil))
      },
      days => cache.fetchAndGetEntry.flatMap {
        case Some(aggregate) =>
          val updatedAggregate = aggregate.copy(salary = Some(Salary(valueInPence, "daily")))
          cache.save(updatedAggregate).map { _ => Redirect(routes.SimpleCalcController.showAgeForm())}
        case None => cache.save(SimpleCalcAggregateInput.newInstance.copy(salary = Some(Salary(valueInPence, "daily")))).map {
          _ =>  Redirect(routes.SimpleCalcController.showAgeForm())
        }
      }
    )
  }

}
