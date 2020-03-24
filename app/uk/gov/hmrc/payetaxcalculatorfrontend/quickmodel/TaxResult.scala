/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.calculator.Calculator
import uk.gov.hmrc.calculator.model.{CalculatorResponse, CalculatorResponsePayPeriod, PayPeriod}
import uk.gov.hmrc.http.BadRequestException

import scala.math.BigDecimal.RoundingMode

case class Tab(tab: String)

object TaxResult {

  val SCOTTISH_TAX_CODE_PREFIX = "SK"

  implicit val format: OFormat[Tab] = Json.format[Tab]

  def tabForm = Form(
    mapping(
      "tab" -> text
    )(Tab.apply)(Tab.unapply)
  )

  def incomeTax(response: CalculatorResponsePayPeriod): BigDecimal =
    response.getTaxToPay

  def extractIncomeTax(response: CalculatorResponsePayPeriod): BigDecimal =
    response.getTaxToPay

  def isOverMaxRate(response: CalculatorResponsePayPeriod): Boolean =
    response.getMaxTaxAmountExceeded

  def taxCalculation(quickCalcAggregateInput: QuickCalcAggregateInput): CalculatorResponse =
    new Calculator(
      extractTaxCode(quickCalcAggregateInput),
      extractSalary(quickCalcAggregateInput).toDouble,
      extractPayPeriod(quickCalcAggregateInput),
      extractOverStatePensionAge(quickCalcAggregateInput),
      extractHours(quickCalcAggregateInput) match {
        case Some(number) => number
        case None         => null
      },
      UserTaxCode.currentTaxYear
    ).run()

  private[quickmodel] def extractTaxCode(quickCalcAggregateInput: QuickCalcAggregateInput): String =
    quickCalcAggregateInput.savedTaxCode match {
      case Some(s) =>
        s.taxCode match {
          case Some(taxCode) => taxCode
          case None          => UserTaxCode.defaultUkTaxCode
        }
      case None => UserTaxCode.defaultUkTaxCode
    }

  private[quickmodel] def extractOverStatePensionAge(quickCalcAggregateInput: QuickCalcAggregateInput): Boolean =
    quickCalcAggregateInput.savedIsOverStatePensionAge match {
      case Some(s) => s.value
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

  private[quickmodel] def extractPayPeriod(quickCalcAggregateInput: QuickCalcAggregateInput): PayPeriod =
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
  private[quickmodel] def extractHours(quickCalcAggregateInput: QuickCalcAggregateInput): Option[Double] =
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

  def moneyFormatter(value: Option[String]): String = {
    val money = """(.*)\.(\d)""".r
    value match {
      case Some(v) =>
        v match {
          case money(pounds, pins) => v + "0"
          case _                   => v
        }
      case _ => ""
    }
  }

  def omitScotland(value: String): String =
    value.replaceAll("Scotland ", "")

}
