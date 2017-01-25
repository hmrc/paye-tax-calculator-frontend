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
import play.api.mvc.Action
import uk.gov.hmrc.payetaxcalculatorfrontend.model.{Forms, QuickCalcUserInput, TaxCalculatorService}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.payetaxcalculatorfrontend.views.html.quickcalc.quick_calc_form

import scala.concurrent.Future

@Singleton
class QuickCalcController @Inject() (override val messagesApi: MessagesApi) extends FrontendController with I18nSupport {

  val QuickCalcUserInputForm: Form[QuickCalcUserInput] = Forms.QuickCalcUserInputForm

  def showForm() = Action { implicit request =>
    Ok(quick_calc_form(QuickCalcUserInputForm,""))
  }

  val calculate = Action.async { implicit request =>

    val userInput = QuickCalcUserInputForm.bindFromRequest()

    val taxCode = userInput.data("taxCode")
    val isStatePensionAge = userInput.data("isStatePensionAge")
    val taxYear = userInput.data("taxYear").toInt
    val grossPay = userInput.data("grossPay").toInt
    val payPeriod = userInput.data("payPeriod")
    val hourlyRate = userInput.data("hourlyRate").toInt
    val hoursPerWeek = userInput.data("hoursPerWeek").toInt

    val result = TaxCalculatorService.calculateTax(isStatePensionAge, taxYear, taxCode, grossPay, payPeriod, hourlyRate)
    Future.successful(Ok(quick_calc_form(QuickCalcUserInputForm,result)))
  }
}
