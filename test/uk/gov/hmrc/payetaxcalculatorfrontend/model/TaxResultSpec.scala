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

package uk.gov.hmrc.payetaxcalculatorfrontend.model

import uk.gov.hmrc.payeestimator.domain.TaxCalc
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.payetaxcalculatorfrontend.model.TaxResult._

/**
  * Created by paul on 02/02/17.
  */
class TaxResultSpec extends UnitSpec {

  "Extracting Tax Code from user response" should {

    "return tax code that a user has provided" in {
      extractTaxCode(QuickCalcAggregateInput(Some(UserTaxCode(true,Some("K452"))), None, None)) shouldBe "K452"
    }

    "return default tax code: 1150L if a user does not provide one" in {
      extractTaxCode(QuickCalcAggregateInput(Some(UserTaxCode(false,None)), None, None)) shouldBe "1150L"
    }
  }

  "Extracting Over65 answer from user response" should {

    "return true if the response is user is over 65" in {
      extractOver65(QuickCalcAggregateInput(None, Some(Over65(true)), None)) shouldBe "true"
    }

    "return false if the reponse is user is not over 65" in {
      extractOver65(QuickCalcAggregateInput(None, Some(Over65(false)), None)) shouldBe "false"
    }

    //Todo unsure about this?
    "return empty string if no response" in {
      extractOver65(QuickCalcAggregateInput(None, None, None)) shouldBe ""
    }
  }

  "Extracting Salary from user response" should {

    "return if response provided is the Yearly Salary" in {
      extractSalary(QuickCalcAggregateInput(None, None, Some(Yearly(20000)))) shouldBe 20000
    }

    "return if response provided is the Monthly Salary" in {
      extractSalary(QuickCalcAggregateInput(None, None, Some(Monthly(2000)))) shouldBe 2000
    }

    "return if response provided is the Weekly Salary" in {
      extractSalary(QuickCalcAggregateInput(None, None, Some(Weekly(200)))) shouldBe 200
    }

    "return if response provided is the Daily Salary" in {
      extractSalary(QuickCalcAggregateInput(None, None, Some(Daily(20,0)))) shouldBe 20
    }

    "return if response provided is the Hourly Salary" in {
      extractSalary(QuickCalcAggregateInput(None, None, Some(Hourly(2, 0)))) shouldBe 2
    }

    "return 0 if no response is provided" in {
      extractSalary(QuickCalcAggregateInput(None, None, None)) shouldBe 0
    }
  }

  "Extracting Pay Period from user response" should {

    "return  if response provided is Yearly" in {
      extractPayPeriod(QuickCalcAggregateInput(None, None, Some(Yearly(0)))) shouldBe "annual"
    }

    "return if response provided is Monthly" in {
      extractPayPeriod(QuickCalcAggregateInput(None, None, Some(Monthly(0)))) shouldBe "monthly"
    }

    "return if response provided is Weekly" in {
      extractPayPeriod(QuickCalcAggregateInput(None, None, Some(Weekly(0)))) shouldBe "weekly"
    }

    "return empty string if response is Daily" in {
      extractPayPeriod(QuickCalcAggregateInput(None, None, Some(Daily(0,0)))) shouldBe ""
    }

    "return empty string if response is Hourly" in {
      extractPayPeriod(QuickCalcAggregateInput(None, None, Some(Hourly(0,0)))) shouldBe ""
    }
  }

  "Extracting Hours from user response" should {

    "return if response is hours in a Daily" in {
      extractHours(QuickCalcAggregateInput(None, None, Some(Daily(0,40)))) shouldBe 40
    }

    "return if response is hours in a Hourly" in {
      extractHours(QuickCalcAggregateInput(None, None, Some(Hourly(0,20)))) shouldBe 20
    }

    "return if response is not Daily or Hourly" in {
      extractHours(QuickCalcAggregateInput(None, None, Some(Weekly(0)))) shouldBe -1
    }
  }

}
