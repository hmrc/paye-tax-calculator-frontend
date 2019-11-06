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

import org.jsoup.Jsoup
import play.api.test.Helpers._

import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.BaseSpec
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._

class ShowResultSpec extends BaseSpec {

  "Show Result Page" should {
    "return 200, with current list of aggregate which contains all answers from previous questions" in {
      val controller = new QuickCalcController(messagesApi, cacheReturnTaxCodeStatePensionSalary, stubControllerComponents())
      val action = controller.showResult()
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      status shouldBe 200
    }

    "return 303, with current list of aggregate data and redirect to Tax Code Form if Tax Code is not provided" in {
      val controller = new QuickCalcController(messagesApi, cacheReturnStatePensionSalary, stubControllerComponents())
      val action = controller.showResult()
      val result = action.apply(request)
      val status = result.header.status

      val actualRedirect = redirectLocation(result).get
      val expectedRedirect = s"${baseURL}tax-code"

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with current list of aggregate data and redirect to Age Form if no answer is provided for \"Are you Over 65?\"" in {
      val controller = new QuickCalcController(messagesApi, cacheReturnTaxCodeSalary, stubControllerComponents())
      val action = controller.showResult()
      val result = action.apply(request)
      val status = result.header.status

      val actualRedirect = redirectLocation(result).get
      val expectedRedirect = s"${baseURL}state-pension"
      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with current list of aggregate data and redirect to Salary Form if Salary is not provided" in {
      val controller = new QuickCalcController(messagesApi, cacheReturnTaxCodeStatePension, stubControllerComponents())
      val action = controller.showResult()
      val result = action.apply(request)
      val status = result.header.status

      val actualRedirect = redirectLocation(result).get
      val expectedRedirect = s"${baseURL}your-pay"

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with empty list of aggregate data and redirect to Tax Code Form" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents())
      val action = controller.showResult()
      val result = action.apply(request)
      val status = result.header.status

      val expectedRedirect = s"${baseURL}your-pay"
      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }
  }
}
