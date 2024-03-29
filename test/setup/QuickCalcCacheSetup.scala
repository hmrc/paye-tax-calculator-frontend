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

package setup

import akka.Done
import forms.YouHaveToldUsItem
import models.{PayPeriodDetail, QuickCalcAggregateInput, Salary, ScottishRate, StatePension, UserTaxCode}
import play.api.i18n.Messages
import services.QuickCalcCache
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

object QuickCalcCacheSetup {

  def cache(mockedResultOfFetching: Option[QuickCalcAggregateInput]) =
    new QuickCalcCache {

      def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] =
        mockedResultOfFetching match {
          case None => Future.successful(None)
          case _    => Future.successful(mockedResultOfFetching)
        }

      def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[Done] =
        Future.successful(Done)
    }

  val baseURL = "/estimate-paye-take-home-pay/"

  val taxCodeTest = YouHaveToldUsItem("1150L", "Tax Code", "/foo", "tax-code")

  val overStatePensionTest =
    YouHaveToldUsItem("YES", "Over 65", "/foo", "StatePensionView")

  val salaryYearlyTest = YouHaveToldUsItem(
    "20000",
    "a_year",
    "/estimate-paye-take-home-pay/your-pay",
    "salary"
  )
  val salaryDailyTest        = YouHaveToldUsItem("40", "a_day", "/foo", "salary")
  val salaryDailyPeriodTest  = YouHaveToldUsItem("5", "Days", "/foo", "time")
  val salaryHourlyPeriodTest = YouHaveToldUsItem("5", "Hours", "/foo", "time")

  val scottishRateTest =
    YouHaveToldUsItem("No", "Scottish", "/foo", "scottish_rate")

  val aggregateListOnlyTaxCode = List(taxCodeTest)

  val aggregateListOnlyTaxCodeAndStatePension =
    List(taxCodeTest, overStatePensionTest)

  val aggregateListTaxCodeStatePensionAndSalary =
    List(taxCodeTest, overStatePensionTest, salaryYearlyTest)

  val aggregateCompleteListYearly =
    List(taxCodeTest, overStatePensionTest, salaryYearlyTest, scottishRateTest)

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

  val cacheTestTaxCode                              = Some(UserTaxCode(false, Some("1250L")))
  val cacheDefaultTestTaxCode                       = Some(UserTaxCode(false, Some("1257L")))
  val cacheTestTaxCodeScottish                      = Some(UserTaxCode(false, Some("S1250L")))
  val cacheTestScottishNO                           = Some(ScottishRate(false, false))
  val cacheTestScottishYES                          = Some(ScottishRate(true, true))
  val cacheTestStatePensionYES                      = Some(StatePension(true))
  val cacheTestStatusPensionNO                      = Some(StatePension(false))
  val cacheTestYearlySalary                         = Some(Salary(20000, "a year", None))
  val cacheTestYearlyOverHundredThoudandSalary      = Some(Salary(100003, "a year", None))
  val cacheTestDailySalary                          = Some(Salary(40, "a day", None))
  val cacheTestHourlySalary                         = Some(Salary(8.5, "an hour", None))
  val cacheTestSalaryPeriodDaily                    = Some(PayPeriodDetail(1, 5, "a day", ""))

  val cacheTestSalaryPeriodHourly = Some(
    PayPeriodDetail(8.5, 40, "an hour", "")
  )

  val cacheTaxCode = Some(
    QuickCalcAggregateInput.newInstance.copy(savedTaxCode = cacheTestTaxCode)
  )

  val cacheTaxCodeStatePension = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES
    )
  )

  val cacheSalaryStatePensionTaxCode = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedTaxCode               = cacheTestTaxCode
    )
  )

  val cacheTaxCodeStatePensionSalary = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheTaxCodeStatePensionSalaryDaily = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestDailySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheCompleteYearly = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheCompleteYearlyScottish = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedTaxCode               = cacheTestTaxCodeScottish,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishYES
    )
  )

  val cacheStatePensionSalary = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedIsOverStatePensionAge = cacheTestStatePensionYES
    )
  )

  val cacheTaxCodeSalary = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary  = cacheTestYearlySalary,
      savedTaxCode = cacheTestTaxCode
    )
  )

  val cacheCompleteDaily = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestDailySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO,
      savedPeriod                = cacheTestSalaryPeriodDaily
    )
  )

  val cacheCompleteHourly = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestHourlySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO,
      savedPeriod                = cacheTestSalaryPeriodHourly
    )
  )

  val cacheShowDisclaimer = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary = cacheTestYearlyOverHundredThoudandSalary,
      savedTaxCode = cacheDefaultTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate = cacheTestScottishNO,
    )
  )

  val cacheEmpty: QuickCalcCache = cache(None)

  val cacheReturnTaxCode: QuickCalcCache = cache(cacheTaxCode)

  val cacheReturnTaxCodeStatePension: QuickCalcCache = cache(
    cacheTaxCodeStatePension
  )

  val cacheReturnTaxCodeStatePensionSalary: QuickCalcCache = cache(
    cacheTaxCodeStatePensionSalary
  )

  val cacheReturnTaxCodeStatePensionSalaryDaily: QuickCalcCache = cache(
    cacheTaxCodeStatePensionSalaryDaily
  )

  val cacheReturnCompleteYearly: QuickCalcCache = cache(cacheCompleteYearly)

  val cacheReturnCompleteDaily: QuickCalcCache = cache(cacheCompleteDaily)

  val cacheReturnCompleteHourly: QuickCalcCache = cache(cacheCompleteHourly)

  val cacheReturnStatePensionSalary: QuickCalcCache = cache(
    cacheStatePensionSalary
  )

  val cacheReturnTaxCodeSalary: QuickCalcCache = cache(cacheTaxCodeSalary)

  val expectedTaxCodeAnswer          = "We have used the default tax code 1250L because you did not enter one."
  val expectedTaxCodeAnswerScottish  = "We have used the default tax code S1250L because you did not enter one."
  val expectedStatePensionYES        = "Yes"
  val expectedStatePensionNO         = "No"
  val expectedYearlySalaryAnswer     = "£20,000 a year"
  val expectedDailySalaryAnswer      = "£40 a day"
  val expectedHourlySalaryAnswer     = "£8.50 an hour"
  val expectedDailyPeriodAnswer      = "5"
  val expectedHourlyPeriodAnswer     = "40"
  val expectedYearlySalaryTypeAnswer = "Per year"
  val expectedScottishAnswer         = "No"
  val expectedScottishAnswerYes      = "Yes"

  val expectedFieldErrorMessage = "This field is required"

  val expectedWrongNumberTaxCodeErrorMessage =
    "Enter your current tax code as numbers and letters, making sure the number is between 0 and 9999"

  val expectedSuffixTaxCodeErrorMessage =
    "Enter a tax code that ends with with the letter L, M, N or T"

  val expectedInvalidTaxCodeErrorMessage =
    "Enter a tax code in the correct format, for example 1117L, K497, S1117L or SK497"

  val expectedPrefixTaxCodeErrorMessage =
    "Enter a tax code that starts with the letters S, K, SK, C or CK followed by numbers"

  val expectedEmptyTaxCodeErrorMessage =
    "Enter your current tax code or change your answer to ‘No’"

  val expectedMinimumNumberErrorMessage =
    "Enter a payment amount that is more than 0"

  val expectedInvalidSalaryErrorMessage =
    "Enter the amount you are paid in the correct format, for example £600 or £193.54"

  val invalidInputErrorMessage =
    "Enter the amount you are paid in the correct format, for example £600 or £193.54"

  val expectedMinHourlyRateErrorMessage = "Hourly rate must be at least 0.01"

  val expectedMinHoursAWeekErrorMessage =
    "Enter hours worked in a week between 1 and 168"

  val expectedMinDailyRateErrorMessage = "Daily rate must be at least 0.01"

  val expectedMinDaysAWeekErrorMessage =
    "Enter a number between 1 and 7"

  val expectedWholeNumberDailyErrorMessage =
    "Enter days worked in a week as a number, like 5 or 4.5"

  val expectedWholeNumberHourlyErrorMessage =
    "Enter hours worked in a week as a number, like 8 or 37.5"

  val expectedMaxHoursAWeekErrorMessage =
    "Enter hours worked in a week between 1 and 168"

  val expectedMaxDaysAWeekErrorMessage =
    "Enter a number between 1 and 7"

  val expectedMaxGrossPayErrorMessage =
    "The amount you are paid must be £9,999,999.99 or less"

  val expectedMaxHourlyRateErrorMessage =
    "Enter your pay as a number less than 10000000.00"

  val disclaimerWarning =
    "Warning We have not reduced your Personal Allowance. If you earn over £100,000, you must enter your tax code for that job to get the most accurate results."

  val expectedEmptyErrorMessage            = "Enter the amount you are paid"

  val expectedPayFrequencyErrorMessage     = "Select how often you are paid"

  val expectedEmptyDaysErrorMessage        = "Enter the number of days a week you work"

  val expectedInvalidStatePensionAnswer    = "Select yes if you are over the State Pension age"

  val expectedInvalidRemoveTaxCodeAnswer    = "Select yes if you want to remove your tax code"
  //Hours
  val expectedEmptyHoursErrorMessage = "Enter the number of hours a week you work"

  //Scottish Rate
  val expectedInvalidScottishRateAnswer = "Select yes if you pay Scottish Income Tax"
}
