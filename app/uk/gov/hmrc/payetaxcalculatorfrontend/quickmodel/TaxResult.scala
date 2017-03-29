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

package uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel

import uk.gov.hmrc.payeestimator.domain.{TaxCalc, TaxCategory}
import uk.gov.hmrc.payeestimator.services.LiveTaxCalculatorService._

object TaxResult {

  private[quickmodel] def extractTaxCode(quickCalcAggregateInput: QuickCalcAggregateInput): String =
    quickCalcAggregateInput.savedTaxCode match {
      case Some(s) => s.taxCode match {
        case Some(taxCode) => taxCode
        case None => UserTaxCode.DEFAULT_TAX_CODE
      }
      case None => UserTaxCode.DEFAULT_TAX_CODE
    }

  private[quickmodel] def extractOverStatePensionAge(quickCalcAggregateInput: QuickCalcAggregateInput): String =
    quickCalcAggregateInput.savedIsOverStatePensionAge match {
      case Some(s) => if (s.value) {
        "true"
      } else {
        "false"
      }
      case None => throw new Exception("No answer has been provided for the question: Are you over state pension age?")
    }

  def extractSalary(quickCalcAggregateInput: QuickCalcAggregateInput): BigDecimal = quickCalcAggregateInput.savedSalary match {
    case Some(s) => s.period match {
      case "yearly" => s.amount * 100
      case "monthly" => s.amount * 100
      case "weekly" => s.amount * 100
      case "daily" => s.amount * 100
      case "hourly" => s.amount * 100
      case _ => throw new Exception("No Salary has been provided.")
    }
    case None => throw new Exception("No Salary has been provided.")
  }

  private[quickmodel] def extractPayPeriod(quickCalcAggregateInput: QuickCalcAggregateInput): String =
    quickCalcAggregateInput.savedSalary match {
      case Some(s) => s.period match {
        case "yearly" => "annual"
        case "monthly" => "monthly"
        case "weekly" => "weekly"
        case _ => ""
      }
      case _ => ""
    }


  /**
    * This function is called "extractHours" because in "buildTaxCalc" function, the last parameter is called "hoursIn".
    * "hoursIn" does not only means hours but can also mean days.
    * buildTaxCalc will use the number returned to calculate the weekly gross pay from Daily or Hourly via those case classes.
    **/
  private[quickmodel] def extractHours(quickCalcAggregateInput: QuickCalcAggregateInput): Int =
    quickCalcAggregateInput.savedSalary match {
      case Some(s) => s.period match {
        case "daily" => s.howManyAWeek.getOrElse(-1)
        case "hourly" => s.howManyAWeek.getOrElse(-1)
        case _ => -1
      }
      case _ => -1
    }

  def incomeTax(maxTaxAmount: BigDecimal, taxCategory: Seq[TaxCategory]): BigDecimal = {
    val stdIncomeTax = extractIncomeTax(taxCategory)
    if(maxTaxAmount >= 0) stdIncomeTax min maxTaxAmount
    else stdIncomeTax
  }

  def extractIncomeTax(taxCategory: Seq[TaxCategory]) = {
    taxCategory.head.total
  }

  def isOverMaxRate(grossPay: BigDecimal, maxTaxRate: BigDecimal, taxablePay: BigDecimal): Boolean = {
    (grossPay * maxTaxRate / 100) < taxablePay
  }

  def taxCalculation(quickCalcAggregateInput: QuickCalcAggregateInput): TaxCalc = {

    val taxCode = extractTaxCode(quickCalcAggregateInput)

    buildTaxCalc(
      extractOverStatePensionAge(quickCalcAggregateInput),
      taxCalcResource = UserTaxCode.taxConfig(taxCode),
      extractTaxCode(quickCalcAggregateInput),
      extractSalary(quickCalcAggregateInput).toInt,
      extractPayPeriod(quickCalcAggregateInput),
      extractHours(quickCalcAggregateInput))
  }

}
