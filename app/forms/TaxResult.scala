/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import com.typesafe.config.Config
import config.AppConfig
import models.{QuickCalcAggregateInput, UserTaxCode}
import uk.gov.hmrc.calculator.Calculator
import uk.gov.hmrc.calculator.model.{CalculatorResponse, CalculatorResponsePayPeriod, PayPeriod}
import uk.gov.hmrc.http.BadRequestException
import utils.DefaultTaxCodeProvider

import javax.inject.Inject
import scala.math.BigDecimal.RoundingMode

object TaxResult {

  val SCOTTISH_TAX_CODE_PREFIX = "SK"

  def incomeTax(response: CalculatorResponsePayPeriod): BigDecimal =
    response.getTaxToPay

  def extractIncomeTax(response: CalculatorResponsePayPeriod): BigDecimal =
    response.getTaxToPay

  def isOverMaxRate(response: CalculatorResponsePayPeriod): Boolean =
    response.getMaxTaxAmountExceeded

  def taxCalculation(
    quickCalcAggregateInput: QuickCalcAggregateInput,
    defaultTaxCodeProvider:  DefaultTaxCodeProvider
  ): CalculatorResponse =
    new Calculator(
      extractTaxCode(quickCalcAggregateInput, defaultTaxCodeProvider),
      extractSalary(quickCalcAggregateInput).toDouble,
      extractPayPeriod(quickCalcAggregateInput),
      extractOverStatePensionAge(quickCalcAggregateInput),
      extractHours(quickCalcAggregateInput) match {
        case Some(number) => number.toDouble
        case None         => null
      }
    ).run()

  def extractTaxCode(
    quickCalcAggregateInput: QuickCalcAggregateInput,
    defaultTaxCodeProvider:  DefaultTaxCodeProvider
  ): String =
    quickCalcAggregateInput.savedTaxCode match {
      case Some(s) =>
        s.taxCode match {
          case Some(taxCode) => taxCode
          case None          => defaultTaxCodeProvider.defaultUkTaxCode
        }
      case None => defaultTaxCodeProvider.defaultUkTaxCode
    }

  def extractOverStatePensionAge(quickCalcAggregateInput: QuickCalcAggregateInput): Boolean =
    quickCalcAggregateInput.savedIsOverStatePensionAge match {
      case Some(s) => s.overStatePensionAge
      case None    => throw new Exception("No answer has been provided for the question: Are you over state pension age?")
    }

  def extractSalary(quickCalcAggregateInput: QuickCalcAggregateInput): BigDecimal =
    quickCalcAggregateInput.savedSalary match {
      case Some(s) =>
        s.period match {
          case "a year"  => s.amount
          case "a month" => s.amount
          case "a week"  => s.amount
          case "a day"   => s.amount
          case "an hour" => s.amount
          case _         => throw new Exception("No Salary has been provided.")
        }
      case None => throw new Exception("No Salary has been provided.")
    }

  def extractPayPeriod(quickCalcAggregateInput: QuickCalcAggregateInput): PayPeriod =
    quickCalcAggregateInput.savedSalary match {
      case Some(s) =>
        s.period match {
          case "a year"  => PayPeriod.YEARLY
          case "a month" => PayPeriod.MONTHLY
          case "a week"  => PayPeriod.WEEKLY
          case "a day"   => PayPeriod.DAILY
          case "an hour" => PayPeriod.HOURLY
          case e         => throw new BadRequestException(s"$e is not a valid PayPeriod")
        }
      case _ => throw new BadRequestException(s"Invalid PayPeriod")
    }

  /**
    * This function is called "extractHours" because in "buildTaxCalc" function, the last parameter is called "hoursIn".
    * "hoursIn" does not only means hours but can also mean days.
    * buildTaxCalc will use the number returned to calculate the weekly gross pay from Daily or Hourly via those case classes.
    **/
  def extractHours(quickCalcAggregateInput: QuickCalcAggregateInput): Option[BigDecimal] =
    quickCalcAggregateInput.savedSalary match {
      case Some(s) =>
        (s.period, s.howManyAWeek) match {
          case ("a day", Some(manyAWeek))   => Some(manyAWeek)
          case ("an hour", Some(manyAWeek)) => Some(manyAWeek)
          case _                            => None
        }
      case _ => None
    }

  def moneyFormatter(value2: BigDecimal): String = {
    val value: BigDecimal = BigDecimal(value2.toDouble).setScale(2, RoundingMode.HALF_UP)
    val formatter = java.text.NumberFormat.getInstance
    val money     = """(.*)\.(\d)""".r
    val outValue  = formatter.format(value)

    outValue match {
      case money(pounds, pins) => {
        formatter.format(value) + "0"
      }
      case _ => formatter.format(value)
    }
  }
}
