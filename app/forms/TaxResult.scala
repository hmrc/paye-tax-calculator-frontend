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

import models.{QuickCalcAggregateInput, UserTaxCode}
import uk.gov.hmrc.calculator.Calculator
import uk.gov.hmrc.calculator.model.pension.AnnualPensionMethod
import uk.gov.hmrc.calculator.model.{BandBreakdown, CalculatorResponse, CalculatorResponsePayPeriod, PayPeriod, TaxYear}
import uk.gov.hmrc.calculator.utils.PayPeriodExtensionsKt
import uk.gov.hmrc.http.BadRequestException
import utils.DefaultTaxCodeProvider
import utils.GetCurrentTaxYear.getTaxYear

import scala.jdk.CollectionConverters._
import scala.math.BigDecimal.RoundingMode

object TaxResult {

  val SCOTTISH_TAX_CODE_PREFIX = "SK"

  def incomeTax(response: CalculatorResponsePayPeriod): BigDecimal =
    response.getTaxToPay

  def extractIncomeTax(response: CalculatorResponsePayPeriod): BigDecimal =
    response.getTaxToPay

  def extractPensionContributions(response: CalculatorResponsePayPeriod): Double =
    response.getPensionContribution

  def incomeTaxBands(response: CalculatorResponsePayPeriod): Map[Double, Double] =
    Option(response.getTaxBreakdown) match {
      case Some(breakdownList) if !breakdownList.isEmpty =>
        breakdownList.asScala.map { band =>
          (band.getPercentage * 100, band.getAmount)
        }.toMap
      case _ =>
        Map.empty
    }

  def isOverMaxRate(response: CalculatorResponsePayPeriod): Boolean =
    response.getMaxTaxAmountExceeded

  def taxCalculation(
    quickCalcAggregateInput: QuickCalcAggregateInput,
    defaultTaxCodeProvider:  DefaultTaxCodeProvider
  ): CalculatorResponse =
    new Calculator(
      extractTaxCode(quickCalcAggregateInput, defaultTaxCodeProvider),
      quickCalcAggregateInput.savedTaxCode.exists(_.gaveUsTaxCode),
      extractSalary(quickCalcAggregateInput).toDouble,
      extractPayPeriod(quickCalcAggregateInput),
      extractOverStatePensionAge(quickCalcAggregateInput),
      extractHours(quickCalcAggregateInput) match {
        case Some(number) => number.toDouble
        case None         => null
      },
      extractTaxYear(getTaxYear),
      extractPensionMethod(quickCalcAggregateInput) match {
        case Some(pensionMethod) => pensionMethod
        case None                => null
      },
      extractPensionYearlyAmount(quickCalcAggregateInput) match {
        case Some(pensionYearlyAmount) => pensionYearlyAmount
        case None                      => null
      },
      extractPensionPercentage(quickCalcAggregateInput) match {
        case Some(pensionPercentageAmount) => pensionPercentageAmount
        case None                          => null
      }
    ).run()

  def convertWagesToYearly(
    wages:             BigDecimal,
    period:            String,
    hoursOrDaysWorked: Option[BigDecimal] = None
  ): BigDecimal = {

    val kotlinPeriod = period match {
      case "a year"        => PayPeriod.YEARLY
      case "a month"       => PayPeriod.MONTHLY
      case "a day"         => PayPeriod.DAILY
      case "an hour"       => PayPeriod.HOURLY
      case "a week"        => PayPeriod.WEEKLY
      case "every 4 weeks" => PayPeriod.FOUR_WEEKLY
      case _               => throw new IllegalArgumentException("Unknown period")
    }
    val result = PayPeriodExtensionsKt
      .convertWageToYearly(wages.toDouble, kotlinPeriod, hoursOrDaysWorked.getOrElse(BigDecimal(0)).toDouble)

    BigDecimal(result)
  }



  def convertWagesToMonthly(wages: BigDecimal): BigDecimal = {

    val result = PayPeriodExtensionsKt
      .convertAmountFromYearlyToPayPeriod(wages.toDouble, PayPeriod.MONTHLY)

    BigDecimal(result)
  }

  private def extractTaxYear(currentTaxYear: Int): TaxYear =
    currentTaxYear match {
      case 2020 => TaxYear.TWENTY_TWENTY
      case 2021 => TaxYear.TWENTY_TWENTY_ONE
      case 2023 => TaxYear.TWENTY_TWENTY_THREE
      case 2024 => TaxYear.TWENTY_TWENTY_FOUR
    }

  private def extractPensionMethod(quickCalcAggregateInput: QuickCalcAggregateInput): Option[AnnualPensionMethod] =
    quickCalcAggregateInput.savedPensionContributions match {
      case Some(s) =>
        s.gaveUsPercentageAmount match {
          case true  => Some(AnnualPensionMethod.PERCENTAGE)
          case false => Some(AnnualPensionMethod.AMOUNT_IN_POUNDS)
          case _           => None
        }
      case _ => None
    }

  private def extractPensionPercentage(quickCalcAggregateInput: QuickCalcAggregateInput): Option[Double] =
    quickCalcAggregateInput.savedPensionContributions match {
      case Some(s) =>
        (s.gaveUsPercentageAmount, s.monthlyContributionAmount) match {
          case (true, Some(contributionAmount)) => Some(contributionAmount.toDouble)
          case _                                      => None
        }
      case _ => None
    }

  private def extractPensionYearlyAmount(quickCalcAggregateInput: QuickCalcAggregateInput): Option[Double] =
    quickCalcAggregateInput.savedPensionContributions match {
      case Some(s) =>
        (s.gaveUsPercentageAmount, s.yearlyContributionAmount) match {
          case (false, Some(contributionAmount)) => Some(contributionAmount.toDouble)
          case _                                       => None
        }
      case _ => None
    }

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
          case "a year"        => s.amount
          case "a month"       => s.amount
          case "a week"        => s.amount
          case "a day"         => s.amount
          case "an hour"       => s.amount
          case "every 4 weeks" => s.amount
          case _               => throw new Exception("No Salary has been provided.")
        }
      case None => throw new Exception("No Salary has been provided.")
    }

  def extractPayPeriod(quickCalcAggregateInput: QuickCalcAggregateInput): PayPeriod =
    quickCalcAggregateInput.savedSalary match {
      case Some(s) =>
        s.period match {
          case "a year"        => PayPeriod.YEARLY
          case "a month"       => PayPeriod.MONTHLY
          case "a week"        => PayPeriod.WEEKLY
          case "a day"         => PayPeriod.DAILY
          case "an hour"       => PayPeriod.HOURLY
          case "every 4 weeks" => PayPeriod.FOUR_WEEKLY
          case e               => throw new BadRequestException(s"$e is not a valid PayPeriod")
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
