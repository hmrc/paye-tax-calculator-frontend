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

import config.AppConfig
import models.{QuickCalcAggregateInput, Salary, StatePension, UserTaxCode}
import org.scalatest.{Tag, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import TaxResult._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import setup.BaseSpec
import uk.gov.hmrc.calculator.model.{CalculatorResponsePayPeriod, PayPeriod}
import uk.gov.hmrc.http.BadRequestException
import utils.DefaultTaxCodeProvider

class TaxResultSpec extends BaseSpec with AnyWordSpecLike {

  "Extracting Tax Code from user response" should {

    "return tax code that a user has provided" in {
      val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = true, Some("K452"))), None)

      extractTaxCode(input, defaultTaxCodeProvider) mustBe "K452"
    }

    "return the default UK tax code for 2022-23 if the user does not provide one" taggedAs Tag("2022") in {
      val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = false, None)), None)

      extractTaxCode(input, defaultTaxCodeProvider) mustBe "1257L"
    }

    "return the default UK tax code for 2023-24 if the user does not provide one" taggedAs Tag("2023") in {
      val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = false, None)), None)

      extractTaxCode(input, defaultTaxCodeProvider) mustBe "1257L"
    }

    "return the default UK tax code for 2023-24 if the user does not provide one and is None" taggedAs Tag(
      "2023"
    ) in {
      val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
      val input = QuickCalcAggregateInput(None, None, None, None, None)

      extractTaxCode(input, defaultTaxCodeProvider) mustBe "1257L"
    }
  }

  "Extracting OverStatePensionAge answer from user response" should {

    "return true if the response is user is over state pension StatePensionView" in {
      val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
      extractOverStatePensionAge(QuickCalcAggregateInput(None, None, Some(StatePension(true)), None, None)) mustBe true
    }

    "return false if the response is user is not over state pension StatePensionView" in {
      val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
      extractOverStatePensionAge(QuickCalcAggregateInput(None, None, Some(StatePension(false)), None, None)) mustBe false
    }

    """return an error with message with "No answer has been provided for the question: Are you over state pension StatePensionView?" if no response""" in {
      val thrown = intercept[Exception] {
        extractOverStatePensionAge(QuickCalcAggregateInput(None, None, None, None, None))
      }
      thrown.getMessage mustBe "No answer has been provided for the question: Are you over state pension age?"
    }
  }

  "Extracting Salary from user response" should {

    "return if response provided is the Yearly Salary: £20000" in {
      extractSalary(
        QuickCalcAggregateInput(Some(Salary(20000, Some(20000), None, "a year", None)), None, None, None, None)
      ) mustBe 20000
    }

    "return if response provided is the Monthly Salary: £2000" in {
      extractSalary(
        QuickCalcAggregateInput(Some(Salary(2000, Some(2000), Some(300), "a month", None)), None, None, None, None)
      ) mustBe 2000
    }

    "return if response provided is the Weekly Salary: £200" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(200, None, None, "a week", None)), None, None, None, None)) mustBe 200
    }

    "return if response provided is the Daily Salary: £20" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(20, None, None, "a day", None)), None, None, None, None)) mustBe 20
    }

    "return if response provided is the Hourly Salary: £2 in pence" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(2, None, None, "an hour", None)), None, None, None, None)) mustBe 2
    }

    """return an error with message "No Salary has been provided" if incorrect salary duration""" in {
      val thrown = intercept[Exception] {
        extractSalary(QuickCalcAggregateInput(Some(Salary(2, None, None, "a decade", None)), None, None, None, None))
      }
      thrown.getMessage mustBe "No Salary has been provided."
    }

    """return an error with message "No Salary has been provided" if no response""" in {
      val thrown = intercept[Exception] {
        extractSalary(QuickCalcAggregateInput(None, None, None, None, None))
      }
      thrown.getMessage mustBe "No Salary has been provided."
    }
  }

  "Extracting Pay Period from user response" should {

    "return  if response provided is Yearly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, None, None, "a year", None)), None, None, None, None)) mustBe PayPeriod.YEARLY
    }

    "return if response provided is Monthly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, None, None, "a month", None)), None, None, None, None)) mustBe PayPeriod.MONTHLY
    }

    "return if response provided is Weekly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, None, None, "a week", None)), None, None, None, None)) mustBe PayPeriod.WEEKLY
    }

    "return empty string if response is Daily" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, None, None, "a day", None)), None, None, None, None)) mustBe PayPeriod.DAILY
    }

    "return empty string if response is Hourly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, None, None, "an hour", None)), None, None, None, None)) mustBe PayPeriod.HOURLY
    }

    "return empty string if response is Every 4 weeks" in {
      extractPayPeriod(
        QuickCalcAggregateInput(Some(Salary(0, None, None, "every 4 weeks", None)), None, None, None, None)
      ) mustBe PayPeriod.FOUR_WEEKLY
    }

    """return an error with message "a decade is not a valid PayPeriod" if incorrect salary duration""" in {
      val thrown = intercept[BadRequestException] {
        extractPayPeriod(QuickCalcAggregateInput(Some(Salary(2, None, None, "a decade", None)), None, None, None, None))
      }
      thrown.getMessage mustBe "a decade is not a valid PayPeriod"
    }

    """return an error with message "Invalid PayPeriod""" in {
      val thrown = intercept[BadRequestException] {
        extractPayPeriod(QuickCalcAggregateInput(None, None, None, None, None))
      }
      thrown.getMessage mustBe "Invalid PayPeriod"
    }

  }

  "Extracting Hours from user response" should {

    "return if response is hours in Daily" in {
      extractHours(QuickCalcAggregateInput(Some(Salary(40, None, None, "a day", Some(4.0))), None, None, None, None)) mustBe Some(
        4.0
      )
    }

    "return if response is hours in Hourly" in {
      extractHours(QuickCalcAggregateInput(Some(Salary(20, None, None, "an hour", Some(10.0))), None, None, None, None)) mustBe Some(
        10.0
      )
    }

    "return if response is not Daily or Hourly" in {
      extractHours(QuickCalcAggregateInput(Some(Salary(-1, None, None, "an hour", None)), None, None, None, None)) mustBe None
    }

    "return None if no salary present" in {
      extractHours(QuickCalcAggregateInput(None, None, None, None, None)) mustBe None
    }
  }

  //"Extracting income tax" should {

//    "return the maxTaxAmount if the tax is over 50% of the gross income" in {
//      val expectedTaxAmount = 10400.00
//      val response =
//        new CalculatorResponsePayPeriod(
//          PayPeriod.YEARLY,
//          expectedTaxAmount,
//          1000.0,
//          500.0,
//          100000.0,
//          null,
//          12509.0,
//          null
//        )
//
//      incomeTax(response) mustBe expectedTaxAmount
//    }
//
//    "return the standard TaxAmount if the tax is not over 50% of the gross income" in {
//      val expectedTaxAmount = 4359.8
//      val response =
//        new CalculatorResponsePayPeriod(
//          PayPeriod.YEARLY,
//          expectedTaxAmount,
//          1000.0,
//          500.0,
//          50000.0,
//          null,
//          12509.0,
//          null
//        )
//
//      incomeTax(response) mustBe expectedTaxAmount
//    }
//  }
//
//  "Check isOverMaxRate or not" should {
//    "return true if income tax is more than 50% of the total (gross) pay entered" in {
//      val response =
//        new CalculatorResponsePayPeriod(PayPeriod.YEARLY, 10000.0, 1000.0, 500.0, 10000.0, null, 12509.0, null)
//
//      isOverMaxRate(response) mustBe true
//    }
//
//    "return false if income tax is not more than 50% of the total (gross) pay entered" in {
//      val response =
//        new CalculatorResponsePayPeriod(PayPeriod.YEARLY, 4999.0, 1000.0, 500.0, 10000.0, null, 12509.0, null)
//
//      isOverMaxRate(response) mustBe false
//    }

//  }

  def newAppForTest(testData: TestData): Application =
    if (testData.tags.contains("2022")) {
      GuiceApplicationBuilder().configure("dateOverride" -> "2022-05-12").build()
    } else if (testData.tags.contains("2023")) {
      GuiceApplicationBuilder().configure("dateOverride" -> "2023-04-06").build()
    } else {
      GuiceApplicationBuilder().configure("dateOverride" -> "2022-04-06").build()
    }
}
