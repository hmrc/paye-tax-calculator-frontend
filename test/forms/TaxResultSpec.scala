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

package forms

import forms.{OverStatePensionAge, Salary, UserTaxCode}
import models.QuickCalcAggregateInput
import org.scalatest.{Tag, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import forms.TaxResult._
import uk.gov.hmrc.calculator.model.{CalculatorResponsePayPeriod, PayPeriod}
import uk.gov.hmrc.play.test.UnitSpec

class TaxResultSpec extends UnitSpec with GuiceOneAppPerTest {

  "Extracting Tax Code from user response" should {

    "return tax code that a user has provided" in {
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = true, Some("K452"))), None)

      extractTaxCode(input) shouldBe "K452"
    }

    "return default tax code for 2018-19 (1150L) if the user does not provide one" taggedAs Tag("2018") in {
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = false, None)), None)

      extractTaxCode(input) shouldBe "1185L"
    }

    "return the default UK tax code for 2019-20 if the user does not provide one" taggedAs Tag("2019") in {
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = false, None)), None)

      extractTaxCode(input) shouldBe "1250L"
    }

    "return the default UK tax code for 2020-21 if the user does not provide one" taggedAs Tag("2020_1") in {
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = false, None)), None)

      extractTaxCode(input) shouldBe "1250L"
    }

    "return the default UK tax code for 2020-21 (if in May onwards as this has new bands for scotland if the user does not provide one" taggedAs Tag(
      "2020_2"
    ) in {
      val input = QuickCalcAggregateInput(None, None, None, Some(UserTaxCode(gaveUsTaxCode = false, None)), None)

      extractTaxCode(input) shouldBe "1250L"
    }
  }

  "Extracting OverStatePensionAge answer from user response" should {

    "return true if the response is user is over state pension state_pension" in {
      extractOverStatePensionAge(QuickCalcAggregateInput(None, None, Some(OverStatePensionAge(true)), None, None)) shouldBe true
    }

    "return false if the response is user is not over state pension state_pension" in {
      extractOverStatePensionAge(QuickCalcAggregateInput(None, None, Some(OverStatePensionAge(false)), None, None)) shouldBe false
    }

    """return an error with message with "No answer has been provided for the question: Are you over state pension state_pension?" if no response""" in {
      val thrown = intercept[Exception] {
        extractOverStatePensionAge(QuickCalcAggregateInput(None, None, None, None, None))
      }
      thrown.getMessage shouldBe "No answer has been provided for the question: Are you over state pension age?"
    }
  }

  "Extracting Salary from user response" should {

    "return if response provided is the Yearly Salary: £20000" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(20000, "a year", None)), None, None, None, None)) shouldBe 20000
    }

    "return if response provided is the Monthly Salary: £2000" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(2000, "a month", None)), None, None, None, None)) shouldBe 2000
    }

    "return if response provided is the Weekly Salary: £200" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(200, "a week", None)), None, None, None, None)) shouldBe 200
    }

    "return if response provided is the Daily Salary: £20" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(20, "a day", None)), None, None, None, None)) shouldBe 20
    }

    "return if response provided is the Hourly Salary: £2 in pence" in {
      extractSalary(QuickCalcAggregateInput(Some(Salary(2, "an hour", None)), None, None, None, None)) shouldBe 2
    }

    """return an error with message "No Salary has been provided" if no response""" in {
      val thrown = intercept[Exception] {
        extractSalary(QuickCalcAggregateInput(None, None, None, None, None))
      }
      thrown.getMessage shouldBe "No Salary has been provided."
    }
  }

  "Extracting Pay Period from user response" should {

    "return  if response provided is Yearly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, "a year", None)), None, None, None, None)) shouldBe PayPeriod.YEARLY
    }

    "return if response provided is Monthly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, "a month", None)), None, None, None, None)) shouldBe PayPeriod.MONTHLY
    }

    "return if response provided is Weekly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, "a week", None)), None, None, None, None)) shouldBe PayPeriod.WEEKLY
    }

    "return empty string if response is Daily" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, "a day", None)), None, None, None, None)) shouldBe PayPeriod.DAILY
    }

    "return empty string if response is Hourly" in {
      extractPayPeriod(QuickCalcAggregateInput(Some(Salary(0, "an hour", None)), None, None, None, None)) shouldBe PayPeriod.HOURLY
    }
  }

  "Extracting Hours from user response" should {

    "return if response is hours in Daily" in {
      extractHours(QuickCalcAggregateInput(Some(Salary(40, "a day", None)), None, None, None, None)) shouldBe None
    }

    "return if response is hours in Hourly" in {
      extractHours(QuickCalcAggregateInput(Some(Salary(20, "an hour", None)), None, None, None, None)) shouldBe None
    }

    "return if response is not Daily or Hourly" in {
      extractHours(QuickCalcAggregateInput(Some(Salary(-1, "an hour", None)), None, None, None, None)) shouldBe None
    }
  }

  "Extracting income tax" should {

    "return the maxTaxAmount if the tax is over 50% of the gross income" in {
      val expectedTaxAmount = 10400.00
      val response =
        new CalculatorResponsePayPeriod(
          PayPeriod.YEARLY,
          expectedTaxAmount,
          1000.0,
          500.0,
          100000.0,
          null,
          12509.0,
          null
        )

      incomeTax(response) shouldBe expectedTaxAmount
    }

    "return the standard TaxAmount if the tax is not over 50% of the gross income" in {
      val expectedTaxAmount = 4359.8
      val response =
        new CalculatorResponsePayPeriod(
          PayPeriod.YEARLY,
          expectedTaxAmount,
          1000.0,
          500.0,
          50000.0,
          null,
          12509.0,
          null
        )

      incomeTax(response) shouldBe expectedTaxAmount
    }
  }

  "Check isOverMaxRate or not" should {
    "return true if income tax is more than 50% of the total (gross) pay entered" in {
      val response =
        new CalculatorResponsePayPeriod(PayPeriod.YEARLY, 10000.0, 1000.0, 500.0, 10000.0, null, 12509.0, null)

      isOverMaxRate(response) shouldBe true
    }

    "return false if income tax is not more than 50% of the total (gross) pay entered" in {
      val response =
        new CalculatorResponsePayPeriod(PayPeriod.YEARLY, 4999.0, 1000.0, 500.0, 10000.0, null, 12509.0, null)

      isOverMaxRate(response) shouldBe false
    }

  }

  override def newAppForTest(testData: TestData): Application =
    if (testData.tags.contains("2020_2")) {
      GuiceApplicationBuilder().configure("dateOverride" -> "2020-05-12").build()
    } else if (testData.tags.contains("2020_1")) {
      GuiceApplicationBuilder().configure("dateOverride" -> "2020-04-06").build()
    } else if (testData.tags.contains("2019")) {
      GuiceApplicationBuilder().configure("dateOverride" -> "2019-04-06").build()
    } else {
      GuiceApplicationBuilder().configure("dateOverride" -> "2018-04-06").build()
    }
}
