/*
 * Copyright 2018 HM Revenue & Customs
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

import org.scalacheck.Gen
import org.scalatest.{FreeSpec, Matchers}
import uk.gov.hmrc.payetaxcalculatorfrontend.config.{EnglandWalesNI, Scotland}
import uk.gov.hmrc.time.TaxYear
import org.scalacheck.Gen.choose
import org.scalatest.prop.PropertyChecks

class DetailedCalcEngineSpec extends FreeSpec with Matchers with PropertyChecks {

  object Engine extends DetailedCalcEngine
  import Engine.calculateTax
  implicit val taxYear2017 = TaxYear(2017)

  "Total income tax should" - {
    "be 0 for earnings < personal allowance" in {
      implicit val region = EnglandWalesNI
      val earnings = Engine.defaultPersonalAllowance - 100
      calculateTax(earnings).totalIncomeTax shouldBe 0
    }
    "include basic rate element for earnings > personal allowance && < (basic rate band + allowance)" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 32000
        calculateTax(earnings).totalIncomeTax shouldBe 4098.20
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 32000
        calculateTax(earnings).totalIncomeTax shouldBe 4098.20
      }
    }
    "include higher rate element for earnings > (basic rate band + allowance) && < additional rate" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 80000
        calculateTax(earnings).totalIncomeTax shouldBe 21010.40
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 80000
        calculateTax(earnings).totalIncomeTax shouldBe 20696.40
      }
    }
    "consider diminishing personal allowance if earnings > tapered allowance limit" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 135000
        calculateTax(earnings).totalIncomeTax shouldBe 47614
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 135000
        calculateTax(earnings).totalIncomeTax shouldBe 47300
      }
    }
    "include additional rate element for earnings > additional rate band" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 160000
        calculateTax(earnings).totalIncomeTax shouldBe 58114
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 160000
        calculateTax(earnings).totalIncomeTax shouldBe 57800
      }
    }
  }

  val positiveEarningsGen: Gen[BigDecimal] = {
    val veryLargeEarnings = 999999999L
    choose(0, veryLargeEarnings).map(BigDecimal.valueOf)
  }

  "For multiple jobs" - {
    "earnings should be summed and calculated as if it was a single source of income" - {
      "for Scotland" in {
        implicit val region = Scotland
        forAll(Gen.listOf(positiveEarningsGen)) { multipleEarnings =>
          calculateTax(multipleEarnings: _*) shouldBe calculateTax(multipleEarnings.sum)
        }
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        forAll(Gen.listOf(positiveEarningsGen)) { multipleEarnings =>
          calculateTax(multipleEarnings: _*) shouldBe calculateTax(multipleEarnings.sum)
        }
      }
    }
    val job1Earnings = 6000
    val job2Earnings = 10000
    val job3Earnings = 80000
    val job4Earnings = 15000
    "pass the examples for English/Welsh" - {
      implicit val region = EnglandWalesNI

      "2) done when total income tax can be calculated for job1 & job2 only" in {
        calculateTax(job1Earnings, job2Earnings).totalIncomeTax shouldBe 898.20
      }
      "3) done when total income tax can be calculated for job1 & job2 & job3 only" in {
        calculateTax(job1Earnings, job2Earnings, job3Earnings).totalIncomeTax shouldBe 27096.40
      }
      "4) done when total income tax can be calculated for job1 & job2 & job3 & job4 only" in {
        calculateTax(job1Earnings, job2Earnings, job3Earnings, job4Earnings).totalIncomeTax shouldBe 35296.40
      }
    }
    "pass the examples for Scottish" - {
      implicit val region = Scotland

      "2) done when total income tax can be calculated for job1 & job2 only" in {
        calculateTax(job1Earnings, job2Earnings).totalIncomeTax shouldBe 898.20
      }
      "3) done when total income tax can be calculated for job1 & job2 & job3 only" in {
        calculateTax(job1Earnings, job2Earnings, job3Earnings).totalIncomeTax shouldBe 27410.40
      }
      "4) done when total income tax can be calculated for job1 & job2 & job3 & job4 only" in {
        calculateTax(job1Earnings, job2Earnings, job3Earnings, job4Earnings).totalIncomeTax shouldBe 35610.40
      }
    }
  }
}
