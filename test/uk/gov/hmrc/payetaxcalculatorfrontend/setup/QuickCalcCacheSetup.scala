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

package uk.gov.hmrc.payetaxcalculatorfrontend.setup

import play.api.i18n.Messages
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel._
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.play.http.HeaderCarrier

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
  val cacheTestYearlySalary = Some(Salary(20000, "yearly", None))
  val cacheTestDailySalary = Some(Salary(40, "daily", None))
  val cacheTestHourlySalary = Some(Salary(8, "hourly", None))
  val cacheTestSalaryPeriodDaily = Some(Detail(5, "daily"))
  val cacheTestSalaryPeriodHourly = Some(Detail(40, "hourly"))

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

  val expectedTaxCodeAnswer = "No (we’ll use the default 1150L)"
  val expectedStatePensionYES = "Yes"
  val expectedStatePensionNO = "No"
  val expectedYearlySalaryAnswer = "£20000"
  val expectedDailySalaryAnswer = "£40"
  val expectedHourlySalaryAnswer = "£8"
  val expectedDailyPeriodAnswer = "5"
  val expectedHourlyPeriodAnswer = "40"
  val expectedYearlySalaryTypeAnswer = "Per year"
  val expectedScottishAnswer = "No"

  val expectedFieldErrorMessage = "This field is required"
  val expectedSuffixTaxCodeErrorMessage = "The tax code you have entered is not valid - it must end with the letter L, M, N, or T"
  val expectedInvalidTaxCodeErrorMessage = "The tax code you have entered is not valid. A tax code is usually made up of several numbers and a letter, e.g. 117L or K497 or S117L or SK497"

  val expectedGrossPayErrorMessage = "Please enter your gross pay"
  val expectedEmptyErrorMessage = "Please enter numbers and \".\" only"
  val expectedNegativeNumberErrorMessage = "The gross pay must be more than zero"
  val expectedInvalidSalaryErrorMessage = "Please enter amount in pounds and pence e.g. 123.45"
  val expectedMinHourlyRateErrorMessage = "Hourly rate must be at least 0.01"
  val expectedMinHoursAWeekErrorMessage = "Hours per week must be at least 1"
  val expectedMinDailyRateErrorMessage = "Daily rate must be at least 0.01"
  val expectedMinDaysAWeekErrorMessage = "Days per week must be at least 1"
  val expectedWholeNumberDailyErrorMessage = "Days per week must be a whole number"
  val expectedWholeNumberHourlyErrorMessage = "Hours per week must be a whole number"
  val expectedMaxHoursAWeekErrorMessage = "Maximum hours per week is 168"
  val expectedMaxDaysAWeekErrorMessage = "Maximum days per week is 7"
  val expectedMaxGrossPayErrorMessage = "Maximum value for gross pay is £9999999.99"
  val expectedMaxHourlyRateErrorMessage = "Maximum value for hourly rate is £9999999.99"
}
