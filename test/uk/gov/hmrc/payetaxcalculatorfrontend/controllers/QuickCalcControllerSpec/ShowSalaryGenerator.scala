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

package uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcControllerSpec

import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._

class ShowSalaryGenerator extends AppUnitGenerator {

  "Show Salary Form" should {
    "return 200, with current list of aggregate data containing Tax Code: 1150L, \"YES\" for is not Over65, 20000 a Year for Salary" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
      val action = csrfAddToken(controller.showTaxCodeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedNumberOfRows = 1 + aggregateListTaxCodeStatePensionAndSalary.size //Including header

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualTaxCode = parseHtml.getElementsByTag("tr").get(1).getElementsByTag("span").get(0).text()
      val actualAgeAnswer = parseHtml.getElementsByTag("tr").get(2).getElementsByTag("span").get(0).text()
      val actualSalary = parseHtml.getElementsByTag("tr").get(3).getElementsByTag("span").get(0).text()
      val actualySalaryType = parseHtml.getElementsByTag("tr").get(3).getElementsByTag("span").get(1).text()

      status shouldBe 200
      actualNumberOfRows shouldBe expectedNumberOfRows
      actualTaxCode shouldBe expectedTaxCode
      actualAgeAnswer shouldBe expectedAgeAnswer
      actualSalary shouldBe expectedSalary
      actualySalaryType shouldBe expectedSalaryType
    }

    "return 200, with empty list of aggregate data" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val action = csrfAddToken(controller.showAgeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size

      status shouldBe 200
      actualNumberOfRows shouldBe 0
    }
  }

}
