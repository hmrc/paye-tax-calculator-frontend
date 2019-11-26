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

package uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcControllerSpec

import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.BaseSpec
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._

class ShowSalarySpec extends BaseSpec {

  "Show Salary Form" should {
    "return 200, with current list of aggregate data containing Tax Code: 1150L, \"YES\" for is not Over65, 20000 a Year for Salary" in {
      val controller =
        new QuickCalcController(messagesApi, cacheReturnTaxCodeStatePensionSalary, stubControllerComponents())
      val action = controller.showSalaryForm()
      val result = action.apply(request)
      val status = result.header.status

      status shouldBe 200
    }

    "return 200, with empty list of aggregate data" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents())
      val action     = controller.showSalaryForm()
      val result     = action.apply(request)
      val status     = result.header.status

      status shouldBe 200
    }
  }

}
