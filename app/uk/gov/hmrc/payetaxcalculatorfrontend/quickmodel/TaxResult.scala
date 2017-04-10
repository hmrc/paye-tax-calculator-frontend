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

import uk.gov.hmrc.payeestimator.domain.{TaxBreakdown, TaxCalc}
import uk.gov.hmrc.payeestimator.services.LiveTaxCalculatorService._

object TaxResult {

  val SCOTTISH_TAX_CODE_PREFIX = "SK"

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
      case "a year" => s.amount * 100
      case "a month" => s.amount * 100
      case "a week" => s.amount * 100
      case "a day" => s.amount * 100
      case "an hour" => s.amount * 100
      case _ => throw new Exception("No Salary has been provided.")
    }
    case None => throw new Exception("No Salary has been provided.")
  }

  private[quickmodel] def extractPayPeriod(quickCalcAggregateInput: QuickCalcAggregateInput): String =
    quickCalcAggregateInput.savedSalary match {
      case Some(s) => s.period match {
        case "a year" => "annual"
        case "a month" => "monthly"
        case "a week" => "weekly"
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
        case "a day" => s.howManyAWeek.getOrElse(-1)
        case "an hour" => s.howManyAWeek.getOrElse(-1)
        case _ => -1
      }
      case _ => -1
    }

  def incomeTax(breakdown : TaxBreakdown): BigDecimal = {
    val maxTaxAmount = breakdown.maxTaxAmount
    val stdIncomeTax: BigDecimal = extractIncomeTax(breakdown)

    incomeTax(maxTaxAmount: BigDecimal, stdIncomeTax)
  }

  def incomeTax(maxTaxAmount: BigDecimal, stdIncomeTax: BigDecimal): BigDecimal = {
    if(maxTaxAmount >= 0) stdIncomeTax min maxTaxAmount
    else stdIncomeTax
  }

  def extractIncomeTax( breakdown : TaxBreakdown): BigDecimal = {
    breakdown.taxCategories.find(_.taxType == "incomeTax").map(_.total)
      .getOrElse(throw new AssertionError("Unknown Engine change"))
  }

  def isOverMaxRate(summary: TaxCalc): Boolean = {
    val grossPay = summary.taxBreakdown.head.grossPay
    val maxTaxRate = summary.maxTaxRate
    val taxablePay = summary.taxBreakdown.head.taxablePay

    isOverMaxRate(grossPay, maxTaxRate, taxablePay)
  }

  def isOverMaxRate(grossPay: BigDecimal, maxTaxRate: BigDecimal, taxablePay: BigDecimal): Boolean = {
    (grossPay * maxTaxRate / 100) <= taxablePay
  }

  def isOverMaxRate(summary: TaxCalc, breakdown: TaxBreakdown): Boolean = {
    val grossPay = summary.taxBreakdown.head.grossPay
    val maxTaxRate = summary.maxTaxRate
    val taxablePay = incomeTax(breakdown)

    isOverMaxRate(grossPay, maxTaxRate, taxablePay)
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

  def moneyFormatter(value: BigDecimal): String ={
    val formatter = java.text.NumberFormat.getInstance
    val money = """(.*)\.(\d)""".r
    val outValue = formatter.format(value)

    outValue match {
      case money(pounds, pins) => {
        formatter.format(value)+"0"
      }
      case _ => formatter.format(value)
    }
  }

  def omitScotland(value: String): String = {
    value.replaceAll("Scotland ", "")
  }

  def getPeriodFromHtml(tab: String): String = {
    tab match {
      case "tabContentmonthly" => ???
      case "tabContentweekly" => ???
      case _ => ???
    }

  }
}
