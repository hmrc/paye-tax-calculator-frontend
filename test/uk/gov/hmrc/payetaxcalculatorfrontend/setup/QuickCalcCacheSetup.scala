/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.setup

import play.api.i18n.Messages
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel._
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object QuickCalcCacheSetup {

  def cache(mockedResultOfFetching: Option[QuickCalcAggregateInput]) = new QuickCalcCache {
    def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
      mockedResultOfFetching match {
        case None => Future.successful(None)
        case _ => Future.successful(mockedResultOfFetching)
      }
    }

    def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = {
      Future.successful(CacheMap("test-empty", Map.empty))
    }
  }

  val baseURL = "/estimate-paye-take-home-pay/"

  val taxCodeTest = YouHaveToldUsItem("1150L", "Tax Code", "/foo", "tax-code")
  val overStatePensionTest = YouHaveToldUsItem("YES", "Over 65", "/foo", "state_pension")
  val salaryYearlyTest = YouHaveToldUsItem("20000", "Per year", "/foo", "salary")
  val salaryDailyTest = YouHaveToldUsItem("40", "Per day", "/foo", "salary")
  val salaryDailyPeriodTest = YouHaveToldUsItem("5", "Days", "/foo", "time")
  val salaryHourlyPeriodTest = YouHaveToldUsItem("5", "Hours", "/foo", "time")
  val scottishRateTest = YouHaveToldUsItem("No", "Scottish", "/foo", "scottish_rate")

  val aggregateListOnlyTaxCode = List(
    taxCodeTest
  )

  val aggregateListOnlyTaxCodeAndStatePension = List(
    taxCodeTest,
    overStatePensionTest
  )

  val aggregateListTaxCodeStatePensionAndSalary = List(
    taxCodeTest,
    overStatePensionTest,
    salaryYearlyTest
  )

  val aggregateCompleteListYearly = List(
    taxCodeTest,
    overStatePensionTest,
    salaryYearlyTest,
    scottishRateTest
  )

  val aggregateCompleteListDaily = List(
    taxCodeTest,
    overStatePensionTest,
    salaryDailyTest,
    scottishRateTest,
    salaryDailyPeriodTest
  )

  val aggregateCompleteListHourly = List(
    taxCodeTest,
    overStatePensionTest,
    salaryDailyTest,
    scottishRateTest,
    salaryHourlyPeriodTest
  )

  val cacheTestTaxCode = Some(UserTaxCode(false, Some("1150L")))
  val cacheTestScottishNO = Some(ScottishRate(false))
  val cacheTestScottishYES = Some(ScottishRate(true))
  val cacheTestStatePensionYES = Some(OverStatePensionAge(true))
  val cacheTestStatusPensionNO = Some(OverStatePensionAge(false))
  val cacheTestYearlySalary = Some(Salary(20000, "a year", None))
  val cacheTestDailySalary = Some(Salary(40, "a day", None))
  val cacheTestHourlySalary = Some(Salary(8, "an hour", None))
  val cacheTestSalaryPeriodDaily = Some(Detail(1, 5, "a day", ""))
  val cacheTestSalaryPeriodHourly = Some(Detail(1, 40, "an hour", ""))

  val cacheTaxCode = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedTaxCode = cacheTestTaxCode
    )
  )

  val cacheTaxCodeStatePension = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedTaxCode = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES
    )
  )

  val cacheTaxCodeStatePensionSalary = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary = cacheTestYearlySalary,
      savedTaxCode = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedScottishRate = cacheTestScottishNO
    )
  )

  val cacheCompleteYearly = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary = cacheTestYearlySalary,
      savedTaxCode = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate = cacheTestScottishNO
    )
  )

  val cacheStatePensionSalary = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary = cacheTestYearlySalary,
      savedIsOverStatePensionAge = cacheTestStatePensionYES
    )
  )

  val cacheTaxCodeSalary = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary = cacheTestYearlySalary,
      savedTaxCode = cacheTestTaxCode
    )
  )

  val cacheCompleteDaily = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary = cacheTestDailySalary,
      savedTaxCode = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate = cacheTestScottishNO,
      savedPeriod = cacheTestSalaryPeriodDaily
    )
  )

  val cacheCompleteHourly = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary = cacheTestHourlySalary,
      savedTaxCode = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate = cacheTestScottishNO,
      savedPeriod = cacheTestSalaryPeriodHourly
    )
  )

  val cacheEmpty: QuickCalcCache = cache(None)

  val cacheReturnTaxCode: QuickCalcCache = cache(cacheTaxCode)

  val cacheReturnTaxCodeStatePension: QuickCalcCache = cache(cacheTaxCodeStatePension)

  val cacheReturnTaxCodeStatePensionSalary: QuickCalcCache = cache(cacheTaxCodeStatePensionSalary)

  val cacheReturnCompleteYearly: QuickCalcCache = cache(cacheCompleteYearly)

  val cacheReturnCompleteDaily: QuickCalcCache = cache(cacheCompleteDaily)

  val cacheReturnCompleteHourly: QuickCalcCache = cache(cacheCompleteHourly)

  val cacheReturnStatePensionSalary: QuickCalcCache = cache(cacheStatePensionSalary)

  val cacheReturnTaxCodeSalary: QuickCalcCache = cache(cacheTaxCodeSalary)

  val expectedTaxCodeAnswer = "No - we’ll use the default (1150L)"
  val expectedStatePensionYES = "Yes"
  val expectedStatePensionNO = "No"
  val expectedYearlySalaryAnswer = "£20,000 a year"
  val expectedDailySalaryAnswer = "£40 a day"
  val expectedHourlySalaryAnswer = "£8 an hour"
  val expectedDailyPeriodAnswer = "5"
  val expectedHourlyPeriodAnswer = "40"
  val expectedYearlySalaryTypeAnswer = "Per year"
  val expectedScottishAnswer = "No"

  val expectedFieldErrorMessage = "This field is required"
  val expectedWrongNumberTaxCodeErrorMessage = "Enter your current tax code as numbers and letters, making sure the number is between 0 and 9999"
  val expectedSuffixTaxCodeErrorMessage = "Enter your current tax code, finishing with the letter L, M, N or T"
  val expectedInvalidTaxCodeErrorMessage = "Enter your current tax code as numbers and letters, for example 1117L, K497, S1117L or SK497"
  val expectedPrefixTaxCodeErrorMessage = "Enter your current tax code, starting with the letter S or K, or the letters SK, followed by numbers"
  val expectedEmptyTaxCodeErrorMessage = "Enter your current tax code or change your answer to ‘No’"

  val expectedEmptyErrorMessage = "Please enter numbers and \".\" only"
  val expectedNegativeNumberErrorMessage = "Enter your pay as a number more than £0.00"
  val expectedInvalidSalaryErrorMessage = "Please enter amount in pounds and pence e.g. 123.45"
  val expectedMinHourlyRateErrorMessage = "Hourly rate must be at least 0.01"
  val expectedMinHoursAWeekErrorMessage = "Enter your hours a week as a number between 1 and 168"
  val expectedMinDailyRateErrorMessage = "Daily rate must be at least 0.01"
  val expectedMinDaysAWeekErrorMessage = "Enter your days a week as a number between 1 and 7"
  val expectedWholeNumberDailyErrorMessage = "Enter your days a week as a whole number"
  val expectedWholeNumberHourlyErrorMessage = "Enter your hours a week as a whole number"
  val expectedMaxHoursAWeekErrorMessage = "Enter your hours a week as a number between 1 and 168"
  val expectedMaxDaysAWeekErrorMessage = "Enter your days a week as a number between 1 and 7"
  val expectedMaxGrossPayErrorMessage = "Enter your pay in pounds and pence. Make sure it’s less than 10000000.00"
  val expectedMaxHourlyRateErrorMessage = "Enter your pay as a number less than 10000000.00"


  //Generic
  def expectedYesNoAnswerErrorMessage(implicit messages: Messages) =
    messages("select_one")

  //Salary
  def expectedInvalidEmptyGrossPayHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.salary.amount_empty_error_link")

  def expectedInvalidGrossPayHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.salary.amount_input_error_link")

  def expectedNotChosenPeriodHeaderMesssage(implicit messages: Messages) =
    messages("quick_calc.salary.option_error_link")

  def expectedEmptyGrossPayErrorMessage(implicit messages: Messages) =
    messages("quick_calc.salary.question.error.empty_salary_input")

  def expectedNotChosenPeriodErrorMessage(implicit messages: Messages) =
    messages("quick_calc.salary.option_error")

  //Days and Hours
  def expectedInvalidPeriodAmountHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.salary.period_error_link_a")

  //Hours
  def expectedEmptyHoursHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.salary.period_error_link_c")

  def expectedEmptyHoursErrorMessage(implicit messages: Messages) =
    messages("quick_calc.salary.question.error.empty_number_hourly")

  //Days
  def expectedEmptyDaysHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.salary.period_error_link_b")

  def expectedEmptyDaysErrorMessage(implicit messages: Messages) =
    messages("quick_calc.salary.question.error.empty_number_daily")

  //Over State Pension
  def expectedInvalidStatePensionAnswerHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.over_state_pension_age_error_link")

  //Tax Code
  def expectedInvalidTaxCodeHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.about_tax_code.wrong_tax_code_error_link")

  def expectedEmptyTaxCodeHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.about_tax_code_error_link")

  def expectedNotAnsweredTaxCodeHeaderMessage(implicit messages: Messages) =
    messages("quick_calc.about_has_tax_code_error_link")
}
