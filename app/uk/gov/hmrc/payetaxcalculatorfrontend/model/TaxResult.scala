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

import uk.gov.hmrc.payeestimator.services.LiveTaxCalculatorService._
import uk.gov.hmrc.time.TaxYearResolver

object TaxResult {

  def extractTaxCode(quickCalcAggregateInput: QuickCalcAggregateInput) = quickCalcAggregateInput.taxCode match {
    case Some(s) => s.taxCode match {
      case Some(taxCode) => taxCode
      case None => UserTaxCode.defaultTaxCode
    }
    case None => UserTaxCode.defaultTaxCode
  }

  def extractOver65(quickCalcAggregateInput: QuickCalcAggregateInput) = quickCalcAggregateInput.isOver65 match {
    case Some(s) => s.value match {
      case true => "true"
      case false => "false"
    }
    case None => throw new Exception("No answer has been provided for the question: Are you Over 65?")
  }

  def extractSalary(quickCalcAggregateInput: QuickCalcAggregateInput) = quickCalcAggregateInput.salary match {
    case Some(s) => s match {
      case s: Yearly => s.value * 100
      case s: Monthly => s.value * 100
      case s: Weekly => s.value * 100
      case s: Daily => s.value * 100
      case s: Hourly => s.value * 100
    }
    case None => throw new Exception("No Salary has been provided.")
  }

  def extractPayPeriod(quickCalcAggregateInput: QuickCalcAggregateInput) = quickCalcAggregateInput.salary match {
    case Some(s) => s match {
      case s: Yearly => "annual"
      case s: Monthly => "monthly"
      case s: Weekly => "weekly"
      case _ => ""
    }
  }

  /**
    * This function is called "extractHours" because in "buildTaxCalc" function, the last parameter is called "hoursIn".
    * "hoursIn" does not only means hours but can also mean days.
    * buildTaxCalc will use the number returned to calculate the weekly gross pay from Daily or Hourly via those case classes.
   **/
  def extractHours(quickCalcAggregateInput: QuickCalcAggregateInput) = quickCalcAggregateInput.salary match {
    case Some(s) => s match {
      case s: Daily => s.howManyDaysAWeek
      case s: Hourly => s.howManyHoursAWeek
      case _ => -1
    }
  }

  def taxCalculation(quickCalcAggregateInput: QuickCalcAggregateInput) = {
    buildTaxCalc(
      extractOver65(quickCalcAggregateInput),
      TaxYearResolver.currentTaxYear,
      extractTaxCode(quickCalcAggregateInput),
      extractSalary(quickCalcAggregateInput).toInt,
      extractPayPeriod(quickCalcAggregateInput),
      extractHours(quickCalcAggregateInput))
  }

}
