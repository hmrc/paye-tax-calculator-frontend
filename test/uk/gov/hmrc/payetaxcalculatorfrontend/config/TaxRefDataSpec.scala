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

package uk.gov.hmrc.payetaxcalculatorfrontend.config

import org.scalatest.Matchers
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.time.TaxYear

class TaxRefDataSpec extends UnitSpec with Matchers {

  "Config for 2017/2018" should {
    "include basic rate for Scotland" in {
      noException shouldBe thrownBy { taxRefData.basicRate(Scotland, taxYear2017) }
    }
    "include basic rate for England, Wales, NI" in {
      noException shouldBe thrownBy { taxRefData.basicRate(EnglandWalesNI, taxYear2017) }
    }
    "include higher rate for Scotland" in {
      noException shouldBe thrownBy { taxRefData.higherRate(Scotland, taxYear2017) }
    }
    "include higher rate for England, Wales, NI" in {
      noException shouldBe thrownBy { taxRefData.higherRate(EnglandWalesNI, taxYear2017) }
    }
    "include additional rate for Scotland" in {
      noException shouldBe thrownBy { taxRefData.additionalRate(Scotland, taxYear2017) }
    }
    "include additional rate for England, Wales, NI" in {
      noException shouldBe thrownBy { taxRefData.additionalRate(EnglandWalesNI, taxYear2017) }
    }
    "include personal allowance" in {
      noException shouldBe thrownBy { taxRefData.defaultPersonalAllowance(taxYear2017) }
    }
    "include tapered allowance limit" in {
      noException shouldBe thrownBy { taxRefData.taperedAllowanceLimit(taxYear2017) }
    }
    "include basic rate band for Scotland" in {
      noException shouldBe thrownBy { taxRefData.basicRateBand(Scotland, taxYear2017) }
    }
    "include basic rate band for England, Wales and NI" in {
      noException shouldBe thrownBy { taxRefData.basicRateBand(EnglandWalesNI, taxYear2017) }
    }
    "include higher rate band for Scotland" in {
      noException shouldBe thrownBy { taxRefData.higherRateBand(Scotland, taxYear2017) }
    }
    "include higher rate band for England, Wales and NI" in {
      noException shouldBe thrownBy { taxRefData.higherRateBand(EnglandWalesNI, taxYear2017) }
    }
    "include additional rate band for Scotland" in {
      noException shouldBe thrownBy { taxRefData.additionalRateBand(Scotland, taxYear2017) }
    }
    "include additional rate band for England, Wales and NI" in {
      noException shouldBe thrownBy { taxRefData.additionalRateBand(EnglandWalesNI, taxYear2017) }
    }
  }

  def taxRefData = new TaxRefData { }
  def taxYear2017 = TaxYear(2017)
}
