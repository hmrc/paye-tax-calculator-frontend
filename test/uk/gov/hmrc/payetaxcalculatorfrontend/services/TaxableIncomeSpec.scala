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

package uk.gov.hmrc.payetaxcalculatorfrontend.services

import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import uk.gov.hmrc.play.test.UnitSpec

class TaxableIncomeSpec extends UnitSpec with Matchers with PropertyChecks {

  "Taxable income" should {
    "be 0 if earnings < default personal allowance" in {
      val earnings = defaultPersonalAllowance - 10
      calcTaxableIncome(earnings) shouldBe 0
    }
    "be equal to earnings - personal allowance if earnings <= tapered allowance limit" in {
      val earnings =
        List(
          defaultPersonalAllowance,
          defaultPersonalAllowance + 50,
          taperedAllowanceLimit
        )

      earnings.foreach { e =>
        calcTaxableIncome(e) shouldBe e - defaultPersonalAllowance
      }
    }


    // for every £2 above limit personal allowance decreases by £1
    "be equal to earnings - allowance where allowance diminishes above limit" in {
      val lim = taperedAllowanceLimit
      val dpa = defaultPersonalAllowance
      val data =
        Table(
          "earnings"      -> "expected taxable income",
          //==========================================
          lim + 10        -> ((lim + 10) - (dpa - (10 / 2))),
          lim + dpa       -> ((lim + dpa) - (dpa - (dpa / 2))),
          lim + 2 * dpa   -> ((lim + 2 * dpa) - (dpa - ( 2 * dpa / 2))), // allowance reduced to 0
          lim + 3 * dpa   -> ((lim + 3 * dpa) - (dpa - ( 2 * dpa / 2))) // allowance doesn't become negative
        )

      forAll(data) { (earnings, expectedTaxableIncome) =>
        calcTaxableIncome(earnings) shouldBe expectedTaxableIncome
      }
    }
    def taperedAllowanceLimit = 1000
    def defaultPersonalAllowance = 100
    def calcTaxableIncome = TaxableIncome.calculate(taperedAllowanceLimit, defaultPersonalAllowance) _

    {
      def taperedAllowanceLimit = 100000
      def defaultPersonalAllowance = 11509

      "pass for example: User Information1.0" in {
        TaxableIncome.calculate(taperedAllowanceLimit, defaultPersonalAllowance)(32000) shouldBe 20491
      }
      "pass for example: User Information3.0" in {
        TaxableIncome.calculate(taperedAllowanceLimit, defaultPersonalAllowance)(135000) shouldBe 135000
      }
      "pass for Extra test for partial personal allowance" in {
        TaxableIncome.calculate(taperedAllowanceLimit, defaultPersonalAllowance)(111000) shouldBe 104991
      }
    }
  }


}
