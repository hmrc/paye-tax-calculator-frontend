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

package uk.gov.hmrc.payetaxcalculatorfrontend.services

import org.scalatest.{FreeSpec, Matchers}
import uk.gov.hmrc.payetaxcalculatorfrontend.config.{EnglandWalesNI, Scotland}
import uk.gov.hmrc.time.TaxYear

class DetailedCalcEngineSpec extends FreeSpec with Matchers {

  object Engine extends DetailedCalcEngine
  implicit val taxYear2017 = TaxYear(2017)

  // values for this spec reflect AC in: https://jira.tools.tax.service.gov.uk/browse/PAYEC-82
  "Total income tax should" - {
    "be 0 for earnings < personal allowance" in {
      implicit val region = EnglandWalesNI
      val earnings = Engine.defaultPersonalAllowance - 100
      Engine.calculateTax(earnings).totalIncomeTax shouldBe 0
    }
    "include basic rate element for earnings > personal allowance && < (basic rate band + allowance)" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 32000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 4098.20
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 32000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 4098.20
      }
    }
    "include higher rate element for earnings > (basic rate band + allowance) && < additional rate" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 80000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 21010.40
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 80000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 20696.40
      }
    }
    "consider diminishing personal allowance if earnings > tapered allowance limit" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 135000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 47614
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 135000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 47300
      }
    }
    "include additional rate element for earnings > additional rate band" - {
      "for Scotland" in {
        implicit val region = Scotland
        val earnings = 160000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 58114
      }
      "for England, Wales, NI" in {
        implicit val region = EnglandWalesNI
        val earnings = 160000
        Engine.calculateTax(earnings).totalIncomeTax shouldBe 57800
      }
    }
  }
}
