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
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.payetaxcalculatorfrontend.model.{QuickCalcAggregateInput, Salary, UserTaxCode, YouHaveToldUsItem}
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.ActionWithSessionId
import uk.gov.hmrc.payetaxcalculatorfrontend.views.html.quickcalc.{quick_calc_form, salary}
import uk.gov.hmrc.play.frontend.controller.FrontendController

@Singleton
class QuickCalcController @Inject() (override val messagesApi: MessagesApi,
                                     cache: QuickCalcCache) extends FrontendController with I18nSupport {

  def showForm() = ActionWithSessionId { implicit request =>
    Ok(quick_calc_form(UserTaxCode.form, List.empty))
  }

  def passTaxCode(url: String) = ActionWithSessionId { implicit request =>

    val userTaxCode = UserTaxCode.form.bindFromRequest

    def userToldUsAboutTaxCode2(userTaxCode: Form[UserTaxCode], url: String): List[YouHaveToldUsItem] = {

      val userToldUsAboutTaxCode = userTaxCode.fold(
        hasErrors = {
          _ =>
            println("======================Error")
            List.empty
        },
        success = {
          theUser =>
            if (!theUser.hasTaxCode) {
              List(YouHaveToldUsItem("1100L", "Tax Code", url))
            } else {
              List(YouHaveToldUsItem(userTaxCode.data("code"), "Tax Code", url))
            }
        }
      )

      userToldUsAboutTaxCode
    }

    val userToldUsAboutTaxCode = userToldUsAboutTaxCode2(userTaxCode, url)

    if (userToldUsAboutTaxCode.isEmpty) {
      val formWithError = UserTaxCode.form.withGlobalError("Please check and re-enter your tax code")
      Ok(quick_calc_form(formWithError, userToldUsAboutTaxCode))
    } else {
      Ok(quick_calc_form(UserTaxCode.form, userToldUsAboutTaxCode))
    }


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
