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

import uk.gov.hmrc.http.cache.client.{CacheMap}

import uk.gov.hmrc.payetaxcalculatorfrontend.model._
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

  val aggregateListOnlyTaxCode = List(YouHaveToldUsItem("1150L", "Tax Code", "/foo","tax-code"))

  val aggregateListOnlyTaxCodeAndStatePension = List(
    YouHaveToldUsItem("1150L", "Tax Code", "/foo","tax-code"),
    YouHaveToldUsItem("YES", "Over 65", "/foo", "age")
  )

  val aggregateListTaxCodeStatePensionAndSalary = List(
    YouHaveToldUsItem("1150L", "Tax Code", "/foo","tax-code"),
    YouHaveToldUsItem("YES", "Over 65", "/foo", "age"),
    YouHaveToldUsItem("20000", "Per year", "/foo", "salary")
  )

  val expectedTaxCode = "1150L"
  val expectedAgeAnswer = "YES"

  val expectedSalary = "£20000"
  val expectedSalaryType = "Per year"

  val expectedFieldErrorMessage = "This field is required"
  val expectedSuffixTaxCodeErrorMessage = "The tax code you have entered is not valid - it must end with the letter L, M, N, or T"
  val expectedInvalidTaxCodeErrorMessage = "The tax code you have entered is not valid. A tax code is usually made up of several numbers and a letter, e.g. 117L or K497 or S117L or SK497"

  val expectedEmptyErrorMessage = "Please enter numbers and \".\" only"
  val expectedNegativeNumberErrorMessage = "The gross pay must be more than zero"
  val expectedInvalidSalaryErrorMessage = "Please enter amount in pounds and pence e.g. 123.45"
  val expectedMinHourlyRateErrorMessage = "Hourly rate must be at least 0.01"
  val expectedMinHoursAWeekErrorMessage = "Hours per week must be at least 1"
  val expectedMinDailyRateErrorMessage = "Daily rate must be at least 0.01"
  val expectedMinDaysAWeekErrorMessage = "Days per week must be at least 1"
  val expectedMaxHoursAWeekErrorMessage= "Maximum hours per week is 168"
  val expectedMaxDaysAWeekErrorMessage = "Maximum days per week is 7"
  val expectedMaxGrossPayErrorMessage = "Maximum value for gross pay is £9,999,999.99"
  val expectedMaxHourlyRateErrorMessage = "Maximum value for hourly rate is £999,9999.99"

  val cacheEmpty = cache(None)

  val cacheReturnTaxCode = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L")))
  )))

  val cacheReturnTaxCodeAndIsOverStatePension = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L"))),
    isOverStatePensionAge = Some(OverStatePensionAge(true)))))

  val cacheReturnTaxCodeIsOverStatePensionAndSalary = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L"))),
    isOverStatePensionAge = Some(OverStatePensionAge(true)),
    salary = Some(Yearly(20000))
  )))

  val cacheReturnNoTaxCodeButAnswerEverythingElse = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    None,
    isOverStatePensionAge = Some(OverStatePensionAge(true)),
    salary = Some(Yearly(20000))
  )))

  val cacheReturnNoAgeButAnswerEverythingElse = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L"))),
    None,
    salary = Some(Yearly(20000))
  )))
}
