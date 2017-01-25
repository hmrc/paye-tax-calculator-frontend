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

package uk.gov.hmrc.payetaxcalculatorfrontend.model

import java.time.LocalDate

import scala.math.BigDecimal
import scala.math.BigDecimal.RoundingMode

object TaxCalculatorService extends TaxCalculatorService

trait TaxCalculatorService {

  def calculateTax(isStatePensionAge: String, taxYear: Int, taxCode: String, grossPayPence: Int, payPeriod: String, hoursIn: Int): String = {

//    val hours = if (hoursIn > 0) Some(hoursIn) else None
//    val updatedPayPeriod = if (hours.getOrElse(-1) > 0) "annual" else payPeriod

    val s = isStatePensionAge+taxYear+taxCode+grossPayPence+payPeriod+hoursIn
    println(s)
    s
  }
}
