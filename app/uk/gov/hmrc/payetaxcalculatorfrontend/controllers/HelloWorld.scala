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

import play.api.data.Form
import play.api.mvc._
import uk.gov.hmrc.payetaxcalculatorfrontend.model.{Forms, TaxCalculatorService, UserInput}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future
import play.api.Play.current

object HelloWorld extends HelloWorld

trait HelloWorld extends FrontendController {
  val userInputForm: Form[UserInput] = Forms.userInputForm

  val helloWorld = Action.async { implicit request =>
		Future.successful(Ok(uk.gov.hmrc.payetaxcalculatorfrontend.views.html.calulatorViews.hello_world(userInputForm,"")))
  }

  val calculate = Action.async { implicit request =>

    val userInput = userInputForm.bindFromRequest()

    val isStatePensionAge = userInput.data("isStatePensionAge")
    val taxYear = userInput.data("taxYear").toInt
    val taxCode = userInput.data("taxCode")
    val grossPayPence = userInput.data("grossPayPence").toInt
    val payPeriod = userInput.data("payPeriod")
    val hoursIn = userInput.data("hoursIn").toInt

    val result = TaxCalculatorService.calculateTax(isStatePensionAge, taxYear, taxCode, grossPayPence, payPeriod, hoursIn)
    Future.successful(Ok(uk.gov.hmrc.payetaxcalculatorfrontend.views.html.calulatorViews.hello_world(userInputForm,result)))
  }

}
