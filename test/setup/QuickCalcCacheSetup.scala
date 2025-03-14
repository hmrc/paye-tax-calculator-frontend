/*
 * Copyright 2024 HM Revenue & Customs
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

import forms._
import models._
import org.apache.pekko.Done
import services.QuickCalcCache
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

object QuickCalcCacheSetup {

  def cache(mockedResultOfFetching: Option[QuickCalcAggregateInput]): QuickCalcCache =
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

  val taxCodeTest: YouHaveToldUsItem = YouHaveToldUsItem("1150L", "Tax Code", "/foo", "tax-code")

  val overStatePensionTest: YouHaveToldUsItem =
    YouHaveToldUsItem("YES", "Over 65", "/foo", "StatePensionView")

  val salaryYearlyTest: YouHaveToldUsItem = YouHaveToldUsItem(
    "20000",
    "a_year",
    "/estimate-paye-take-home-pay/your-pay",
    "salary"
  )
  val salaryDailyTest:        YouHaveToldUsItem = YouHaveToldUsItem("40", "a_day", "/foo", "salary")
  val salaryDailyPeriodTest:  YouHaveToldUsItem = YouHaveToldUsItem("5", "Days", "/foo", "time")
  val salaryHourlyPeriodTest: YouHaveToldUsItem = YouHaveToldUsItem("5", "Hours", "/foo", "time")

  val scottishRateTest: YouHaveToldUsItem =
    YouHaveToldUsItem("No", "Scottish", "/foo", "scottish_rate")

  val aggregateListOnlyTaxCode: List[YouHaveToldUsItem] = List(taxCodeTest)

  val aggregateListOnlyTaxCodeAndStatePension: List[YouHaveToldUsItem] =
    List(taxCodeTest, overStatePensionTest)

  val aggregateListTaxCodeStatePensionAndSalary: List[YouHaveToldUsItem] =
    List(taxCodeTest, overStatePensionTest, salaryYearlyTest)

  val aggregateCompleteListYearly: List[YouHaveToldUsItem] =
    List(taxCodeTest, overStatePensionTest, salaryYearlyTest, scottishRateTest)

  val aggregateCompleteListDaily: Seq[YouHaveToldUsItem] = List(
    taxCodeTest,
    overStatePensionTest,
    salaryDailyTest,
    scottishRateTest,
    salaryDailyPeriodTest
  )

  val aggregateCompleteListHourly: Seq[YouHaveToldUsItem] = List(
    taxCodeTest,
    overStatePensionTest,
    salaryDailyTest,
    scottishRateTest,
    salaryHourlyPeriodTest
  )

  val cacheTestTaxCode:                         Option[UserTaxCode]     = Some(UserTaxCode(gaveUsTaxCode = false, Some("1250L")))
  val cacheDefaultTestTaxCode:                  Option[UserTaxCode]     = Some(UserTaxCode(gaveUsTaxCode = true, Some("1257L")))
  val cacheTestTaxCodeScottish:                 Option[UserTaxCode]     = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1250L")))
  val cacheTestScottishNO:                      Option[ScottishRate]    = Some(ScottishRate(payScottishRate = Some(false)))
  val cacheTestScottishYES:                     Option[ScottishRate]    = Some(ScottishRate(payScottishRate = Some(true)))
  val cacheTestStatePensionYES:                 Option[StatePension]    = Some(StatePension(true))
  val cacheTestStatusPensionNO:                 Option[StatePension]    = Some(StatePension(false))
  val cacheTestYearlySalary:                    Option[Salary]          = Some(Salary(20000, None, None, Yearly, None, None))
  val cacheTestYearlySalaryLessThan100k:        Option[Salary]          = Some(Salary(90000, None, None, Yearly, None, None))
  val cacheTestFourWeeklySalary:                Option[Salary]          = Some(Salary(20000, None, None, FourWeekly, None, None))
  val cacheTestYearlyOverHundredThoudandSalary: Option[Salary]          = Some(Salary(100003, None, None, Yearly, None, None))
  val cacheTestDailySalary:                     Option[Salary]          = Some(Salary(40, None, None, Daily, None, None))
  val cacheTestHourlySalary:                    Option[Salary]          = Some(Salary(8.5, None, None, Hourly, None, None))
  val cacheTestSalaryPeriodDaily:               Option[PayPeriodDetail] = Some(PayPeriodDetail(1, 5, "a day", ""))

  val cacheTestPensionFixedContributions: Option[PensionContributions] = Some(
    PensionContributions(gaveUsPercentageAmount = false, Some(50), Some(600))
  )

  val cacheStudentLoanContributions: Option[StudentLoanContributions] = Some(
    StudentLoanContributions(Some(PlanOne))
  )

  val cachePostGradLoanContributions: Option[PostgraduateLoanContributions] = Some(
    PostgraduateLoanContributions(Some(true))
  )

  val cacheTestPensionPecentageContributions: Option[PensionContributions] = Some(
    PensionContributions(gaveUsPercentageAmount = true, Some(50), None)
  )

  val cacheTestSalaryPeriodHourly: Option[PayPeriodDetail] = Some(
    PayPeriodDetail(8.5, 40, "an hour", "")
  )

  val cacheTaxCode: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(savedTaxCode = cacheTestTaxCode)
  )

  val cacheTaxCodeStatePension: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES
    )
  )

  val cacheSalaryStatePensionTaxCode: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedTaxCode               = cacheTestTaxCode
    )
  )

  val cacheSalaryTaxCodeSavedPensionContributionsFixed: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedTaxCode               = cacheTestTaxCode,
      savedScottishRate          = cacheTestScottishNO,
      savedPensionContributions  = cacheTestPensionFixedContributions
    )
  )

  val cacheSalaryTaxCodeSavedPensionContributionsPercentage: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedTaxCode               = cacheTestTaxCode,
      savedScottishRate          = cacheTestScottishNO,
      savedPensionContributions  = cacheTestPensionPecentageContributions
    )
  )

  val cacheTaxCodeStatePensionSalary: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheTaxCodeStatePensionSalaryLessThan100k: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalaryLessThan100k,
      savedTaxCode               = cacheDefaultTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheTaxCodeStatePensionSalaryLessThan100kWithStudentLoan: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalaryLessThan100k,
      savedTaxCode               = cacheDefaultTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO,
      savedStudentLoanContributions = cacheStudentLoanContributions
    )
  )

  val cacheTaxCodeStatePensionSalaryLessThan100kWithScottishTax: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalaryLessThan100k,
      savedTaxCode               = cacheDefaultTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishYES
    )
  )

  val cacheTaxCodeStatePensionSalaryMoreThan100k: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlyOverHundredThoudandSalary,
      savedTaxCode               = cacheDefaultTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCode: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlyOverHundredThoudandSalary,
      savedTaxCode               = None,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCodeWithPension: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlyOverHundredThoudandSalary,
      savedTaxCode               = None,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO,
      savedPensionContributions  = cacheTestPensionPecentageContributions
    )
  )

  val cacheTaxCodeStatePensionSalaryFourWeekly: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestFourWeeklySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheTaxCodeStatePensionSalaryStudentLoan: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                   = cacheTestYearlySalary,
      savedTaxCode                  = cacheTestTaxCode,
      savedIsOverStatePensionAge    = cacheTestStatePensionYES,
      savedScottishRate             = cacheTestScottishNO,
      savedStudentLoanContributions = cacheStudentLoanContributions
    )
  )

  val cacheTaxCodeStatePensionSalaryStudentLoanPostGrad: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                        = cacheTestYearlySalary,
      savedTaxCode                       = cacheTestTaxCode,
      savedIsOverStatePensionAge         = cacheTestStatePensionYES,
      savedScottishRate                  = cacheTestScottishNO,
      savedStudentLoanContributions      = cacheStudentLoanContributions,
      savedPostGraduateLoanContributions = cachePostGradLoanContributions
    )
  )

  val cacheTaxCodeStatePensionSalaryDaily: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestDailySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatePensionYES,
      savedScottishRate          = cacheTestScottishNO
    )
  )

  val cacheCompleteYearly: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                        = cacheTestYearlySalary,
      savedTaxCode                       = cacheTestTaxCode,
      savedIsOverStatePensionAge         = cacheTestStatusPensionNO,
      savedScottishRate                  = cacheTestScottishNO,
      savedPensionContributions          = cacheTestPensionPecentageContributions,
      savedStudentLoanContributions      = cacheStudentLoanContributions,
      savedPostGraduateLoanContributions = cachePostGradLoanContributions
    )
  )

  val cacheCompleteYearlyScottish: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedTaxCode               = cacheTestTaxCodeScottish,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishYES
    )
  )

  val cacheStatePensionSalary: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlySalary,
      savedIsOverStatePensionAge = cacheTestStatePensionYES
    )
  )

  val cacheTaxCodeSalary: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary  = cacheTestYearlySalary,
      savedTaxCode = cacheTestTaxCode
    )
  )

  val cacheCompleteDaily: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestDailySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO,
      savedPeriod                = cacheTestSalaryPeriodDaily
    )
  )

  val cacheCompleteHourly: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestHourlySalary,
      savedTaxCode               = cacheTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO,
      savedPeriod                = cacheTestSalaryPeriodHourly
    )
  )

  val cacheShowDisclaimer: Option[QuickCalcAggregateInput] = Some(
    QuickCalcAggregateInput.newInstance.copy(
      savedSalary                = cacheTestYearlyOverHundredThoudandSalary,
      savedTaxCode               = cacheDefaultTestTaxCode,
      savedIsOverStatePensionAge = cacheTestStatusPensionNO,
      savedScottishRate          = cacheTestScottishNO
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

  val expectedInvalidPensionsErrorMessage =
    "Enter your monthly pension contributions amount in the correct format"

  val expectedInvalidStudentLoanErrorMessage =
    "Select which student loan you are currently repaying"

  val expectedInvalidPostgraduateLoanErrorMessage =
    "Select yes if you repay a postgraduate loan"

  val expectedInvalidPensionTwoDecimalPlaces =
    "Your monthly pension contributions can only include pounds and pence"

  val expectedInvalidPensionMessageOverTenMill =
    "Your monthly pension contributions must be £9,999,999.99 or less"

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

  val expectedEmptyErrorMessage = "Enter the amount you are paid"

  val expectedPayFrequencyErrorMessage = "Select how often you are paid"

  val expectedEmptyDaysErrorMessage = "Enter the number of days a week you work"

  val expectedInvalidStatePensionAnswer = "Select yes if you are over the State Pension age"

  val expectedInvalidRemoveTaxCodeAnswer = "Select yes if you want to remove your tax code"
  //Hours
  val expectedEmptyHoursErrorMessage = "Enter the number of hours a week you work"

  //Scottish Rate
  val expectedInvalidScottishRateAnswer = "Select yes if you pay Scottish Income Tax"
}
