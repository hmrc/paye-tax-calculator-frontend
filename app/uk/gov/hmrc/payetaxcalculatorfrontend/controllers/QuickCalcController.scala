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

import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import uk.gov.hmrc.payetaxcalculatorfrontend.model._
import uk.gov.hmrc.payetaxcalculatorfrontend.model.UserTaxCode._
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.ActionWithSessionId
import uk.gov.hmrc.payetaxcalculatorfrontend.views.html.quickcalc.{age, quick_calc_form, salary}
import uk.gov.hmrc.play.frontend.controller.FrontendController


@Singleton
class QuickCalcController @Inject() (override val messagesApi: MessagesApi,
                                     cache: QuickCalcCache) extends FrontendController with I18nSupport {

  def showTaxCodeForm() = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry.map {
      case Some(aggregate) =>
        val form = aggregate.taxCode.map(UserTaxCode.form.fill).getOrElse(UserTaxCode.form)
        Ok(quick_calc_form(form, aggregate.youHaveToldUsItems))
      case None =>
        Ok(quick_calc_form(UserTaxCode.form, Nil))
    }
  }

  def submitTaxCodeForm() = ActionWithSessionId.async { implicit request =>

    UserTaxCode.form.bindFromRequest.fold(
      formWithErrors => cache.fetchAndGetEntry.map {
        case Some(aggregate) => BadRequest(quick_calc_form(UserTaxCode.form.withGlobalError(
          Messages("quick_calc.about_tax_code.wrong_tax_code")), aggregate.youHaveToldUsItems))
        case None => BadRequest(quick_calc_form(UserTaxCode.form.withGlobalError(
          Messages("quick_calc.about_tax_code.wrong_tax_code")), Nil))
      },
      newTaxCode => cache.fetchAndGetEntry.flatMap {
        case Some(aggregate) =>
          val updatedTaxCode = if (newTaxCode.hasTaxCode) newTaxCode else UserTaxCode(hasTaxCode = false, Some(defaultTaxCode))
          val newAggregate = aggregate.copy(taxCode = Some(updatedTaxCode))
          cache.save(newAggregate).map {
          _ => Ok(age(Over65.form, newAggregate.youHaveToldUsItems))
        }
        case None =>
          val aggregate = QuickCalcAggregateInput.newInstance.copy(taxCode = Some(newTaxCode))
          cache.save(aggregate).map {
          _ => Ok(age(Over65.form, aggregate.youHaveToldUsItems))
        }
      }
    )

  }

  def showAgeForm() = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry.map {
      case Some(aggregate) =>
        val form = aggregate.isOver65.map(Over65.form.fill).getOrElse(Over65.form)
        Ok(age(form, aggregate.youHaveToldUsItems))
      case None =>
        // TODO if aggregate unavailable here user bypassed tax-code selection which is wrong
        Ok(age(Over65.form, Nil))
    }
  }

  def submitAgeForm() = ActionWithSessionId.async { implicit request =>

    Over65.form.bindFromRequest.fold(
      formWithErrors => cache.fetchAndGetEntry.map {
        case Some(aggregate) => BadRequest(age(formWithErrors, aggregate.youHaveToldUsItems))
        case None => BadRequest(age(formWithErrors, Nil))
      },
      userAge => cache.fetchAndGetEntry.flatMap {
        case Some(aggregate) =>
          val updatedAggregate = aggregate.copy(isOver65 = Some(userAge))
          cache.save(updatedAggregate).map {
          _ => Ok(salary(Salary.form, updatedAggregate.youHaveToldUsItems))
        }
        case None => cache.save(QuickCalcAggregateInput.newInstance.copy(isOver65 = Some(userAge))).map {
          _ => Ok(salary(Salary.form, List.empty))
        }
      }
    )
  }

  def showSalaryForm() = ActionWithSessionId.async { implicit request =>
    cache.fetchAndGetEntry.map {
      case Some(aggregate) =>
        val form = aggregate.salary.map(Salary.form.fill).getOrElse(Salary.form)
        Ok(salary(form, aggregate.youHaveToldUsItems))
      case None =>
        // TODO if aggregate unavailable here user bypassed tax-code selection which is wrong
        Ok(salary(Salary.form, Nil))
    }
  }

  def submitSalaryForm() = ActionWithSessionId.async { implicit request =>
    Salary.form.bindFromRequest.fold(
      formWithErrors => cache.fetchAndGetEntry.map {
        case Some(aggregate) => BadRequest(salary(formWithErrors, aggregate.youHaveToldUsItems))
        case None => BadRequest(salary(formWithErrors, Nil))
      },
      newSalary => cache.fetchAndGetEntry.flatMap {
        case Some(aggregate) => cache.save(aggregate.copy(salary = Some(newSalary))).map {
          _ => Ok("next page goes here")
        }
        case None => cache.save(QuickCalcAggregateInput.newInstance.copy(salary = Some(newSalary))).map {
          _ => Ok("next page goes here")
        }
      }
    )
  }

}
