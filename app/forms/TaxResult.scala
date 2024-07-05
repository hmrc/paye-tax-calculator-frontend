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

import models.QuickCalcAggregateInput
import uk.gov.hmrc.calculator.Calculator
import uk.gov.hmrc.calculator.Calculator.{PensionContribution, StudentLoanPlans}
import uk.gov.hmrc.calculator.model.pension.PensionMethod
import uk.gov.hmrc.calculator.model.{CalculatorResponse, CalculatorResponsePayPeriod, PayPeriod, TaxYear}
import uk.gov.hmrc.calculator.utils.WageConverterUtils
import uk.gov.hmrc.http.BadRequestException
import utils.DefaultTaxCodeProvider
import utils.GetCurrentTaxYear.getTaxYear

import scala.jdk.CollectionConverters._
import scala.math.BigDecimal.RoundingMode

object TaxResult {

  def extractIncomeTax(response: CalculatorResponsePayPeriod): BigDecimal =
    response.getTaxToPay

  def incomeTaxBands(response: CalculatorResponsePayPeriod): Map[Double, Double] =
    Option(response.getTaxBreakdown) match {
      case Some(breakdownList) if !breakdownList.isEmpty =>
        breakdownList.asScala.map { band =>
          (band.getPercentage * 100, band.getAmount)
        }.toMap
      case _ =>
        Map.empty
    }

  private def extractUserPaysScottishTax(quickCalcAggregateInput: QuickCalcAggregateInput): Boolean =
    quickCalcAggregateInput.savedScottishRate match {
      case Some(s) => s.payScottishRate
      case None    => false
    }

  def taxCalculation(
    quickCalcAggregateInput: QuickCalcAggregateInput,
    defaultTaxCodeProvider:  DefaultTaxCodeProvider
  ): CalculatorResponse =
    new Calculator(
      extractTaxCode(quickCalcAggregateInput, defaultTaxCodeProvider),
      extractUserPaysScottishTax(quickCalcAggregateInput),
      quickCalcAggregateInput.savedTaxCode.exists(_.gaveUsTaxCode),
      extractSalary(quickCalcAggregateInput).toDouble,
      extractPayPeriod(quickCalcAggregateInput),
      extractOverStatePensionAge(quickCalcAggregateInput),
      extractHours(quickCalcAggregateInput) match {
        case Some(number) => number.toDouble
        case None         => null
      },
      extractTaxYear(getTaxYear),
      extractPensionContributions(quickCalcAggregateInput) match {
        case Some(pensionContribution) => pensionContribution
        case None                      => null
      },
      extractStudentLoanContributions(quickCalcAggregateInput) match {
        case Some(studentLoanContributions) => studentLoanContributions
        case None                           => null
      }
    ).run()

  def convertWagesToYearly(
    wages:             BigDecimal,
    period:            String,
    hoursOrDaysWorked: Option[BigDecimal] = None
  ): BigDecimal = {

    val result = period match {
      case "a year"  => wages.toDouble
      case "a month" => WageConverterUtils.INSTANCE.convertMonthlyWageToYearly(wages.toDouble)
      case "a day" =>
        WageConverterUtils.INSTANCE.convertDailyWageToYearly(
          wages.toDouble,
          hoursOrDaysWorked.map(_.toDouble).getOrElse(throw new Exception("Not supplied days"))
        )
      case "an hour" =>
        WageConverterUtils.INSTANCE.convertHourlyWageToYearly(
          wages.toDouble,
          hoursOrDaysWorked.map(_.toDouble).getOrElse(throw new Exception("Not supplied hours"))
        )
      case "a week"        => WageConverterUtils.INSTANCE.convertWeeklyWageToYearly(wages.toDouble)
      case "every 4 weeks" => WageConverterUtils.INSTANCE.convertFourWeeklyWageToYearly(wages.toDouble)
    }

    BigDecimal(result)
  }

  private def extractPensionContributions(
    quickCalcAggregateInput: QuickCalcAggregateInput
  ): Option[PensionContribution] =
    (quickCalcAggregateInput.savedPensionContributions.map(_.gaveUsPercentageAmount),
     quickCalcAggregateInput.savedPensionContributions
       .flatMap(_.monthlyContributionAmount)
       .getOrElse(BigDecimal(0))) match {
      case (Some(true), amount) => Some(new PensionContribution(PensionMethod.PERCENTAGE, amount.toDouble))
      case (Some(false), amount) =>
        Some(new PensionContribution(PensionMethod.MONTHLY_AMOUNT_IN_POUNDS, amount.toDouble))
      case _ => None
    }

  private def extractStudentLoanContributions(
    quickCalcAggregateInput: QuickCalcAggregateInput
  ): Option[StudentLoanPlans] =
    (quickCalcAggregateInput.savedStudentLoanContributions.map(_.studentLoanPlan) match {
      case Some("plan one") =>
        Some(new StudentLoanPlans(true, false, false, extractPostGradLoan(quickCalcAggregateInput)))
      case Some("plan two") =>
        Some(new StudentLoanPlans(false, true, false, extractPostGradLoan(quickCalcAggregateInput)))
      case Some("plan four") =>
        Some(new StudentLoanPlans(false, false, true, extractPostGradLoan(quickCalcAggregateInput)))
      case Some("none of these") =>
        Some(new StudentLoanPlans(false, false, false, extractPostGradLoan(quickCalcAggregateInput)))
      case _ => None
    })

  private def extractPostGradLoan(quickCalcAggregateInput: QuickCalcAggregateInput): Boolean =
    quickCalcAggregateInput.savedPostGraduateLoanContributions.map(_.hasPostgraduatePlan) match {
      case Some(true) => true
      case _          => false
    }

  def convertWagesToMonthly(wages: BigDecimal): BigDecimal =
    BigDecimal(WageConverterUtils.INSTANCE.convertYearlyWageToMonthly(wages.toDouble))

  def extractTaxYear(currentTaxYear: Int): TaxYear =
    currentTaxYear match {
      case 2020 => TaxYear.TWENTY_TWENTY
      case 2021 => TaxYear.TWENTY_TWENTY_ONE
      case 2023 => TaxYear.TWENTY_TWENTY_THREE
      case 2024 => TaxYear.TWENTY_TWENTY_FOUR
    }

  def extractTaxCode(
    quickCalcAggregateInput: QuickCalcAggregateInput,
    defaultTaxCodeProvider:  DefaultTaxCodeProvider
  ): String = {
    val taxCode = quickCalcAggregateInput.savedTaxCode match {
      case Some(s) =>
        s.taxCode match {
          case Some(taxCode) => taxCode
          case None          => if (quickCalcAggregateInput.savedScottishRate.exists(_.payScottishRate)) {
            defaultTaxCodeProvider.defaultScottishTaxCode
          } else {
            defaultTaxCodeProvider.defaultUkTaxCode
          }
        }
      case None => defaultTaxCodeProvider.defaultUkTaxCode
    }
    taxCode
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
      case money(_, _) => {
        formatter.format(value) + "0"
      }
      case _ => formatter.format(value)
    }
  }
}
