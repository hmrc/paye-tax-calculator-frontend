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
import play.filters.csrf.CSRFFilter
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._

class ShowSummarySpec extends AppUnitGenerator {

  "Show Summary Page" should {

    "return aggregate data of : Earning £20000 Yearly Salary, NOT (Over State Pension), Tax Code: S1150L and IS Scottish Tax Payer"  in {

      val controller = new QuickCalcController(messages.messages, cacheReturnCompleteYearly)(new CSRFFilter)
      val action = csrfAddToken(controller.summary())
      val result = action.apply(request.withSession("csrfToken" -> "someToken"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedTableSize = 5 // Including header


      val actualTableSize = parseHtml.getElementById("content").getElementsByTag("tr").size()

      val actualSalary = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(1).getElementsByTag("td").get(1).text()

      val actualStatePensionAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(2).getElementsByTag("td").get(1).text()

      val actualTaxCodeAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(3).getElementsByTag("td").get(1).text()

      val actualScottishAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(4).getElementsByTag("td").get(1).text()

      status shouldBe 200
      actualTableSize shouldBe expectedTableSize
      actualSalary shouldBe expectedYearlySalaryAnswer
      actualStatePensionAnswer shouldBe expectedStatePensionNO
      actualTaxCodeAnswer shouldBe expectedTaxCodeAnswer
      actualScottishAnswer shouldBe expectedScottishAnswer
    }

    "return aggregate data of : Earning £40 Daily Salary, 5 Days a Week, NOT (Over State Pension), Tax Code: 1150L and is NOT Scottish Tax Payer"  in {

      val controller = new QuickCalcController(messages.messages, cacheReturnCompleteDaily)(new CSRFFilter)
      val action = csrfAddToken(controller.summary())
      val result = action.apply(request.withSession("csrfToken" -> "someToken"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedTableSize = 6 // Including header

      val actualTableSize = parseHtml.getElementById("content").getElementsByTag("tr").size()

      val actualSalary = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(1).getElementsByTag("td").get(1).text()

      val actualSalaryPeriod = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(2).getElementsByTag("td").get(1).text()

      val actualStatePensionAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(3).getElementsByTag("td").get(1).text()

      val actualTaxCodeAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(4).getElementsByTag("td").get(1).text()

      val actualScottishAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(5).getElementsByTag("td").get(1).text()


      status shouldBe 200
      actualTableSize shouldBe expectedTableSize
      actualSalary shouldBe expectedDailySalaryAnswer
      actualSalaryPeriod shouldBe expectedDailyPeriodAnswer
      actualStatePensionAnswer shouldBe expectedStatePensionNO
      actualTaxCodeAnswer shouldBe expectedTaxCodeAnswer
      actualScottishAnswer shouldBe expectedScottishAnswer
    }

    "return aggregate data of : Earning £8 Hourly Salary, YES (Over State Pension), Tax Code: 1150L and is NOT Scottish Tax Payer"  in {

      val controller = new QuickCalcController(messages.messages, cacheReturnCompleteHourly)(new CSRFFilter)
      val action = csrfAddToken(controller.summary())
      val result = action.apply(request.withSession("csrfToken" -> "someToken"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedTableSize = 6 // Including header

      val actualTableSize = parseHtml.getElementById("content").getElementsByTag("tr").size()

      val actualSalary = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(1).getElementsByTag("td").get(1).text()

      val actualSalaryPeriod = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(2).getElementsByTag("td").get(1).text()

      val actualStatePensionAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(3).getElementsByTag("td").get(1).text()

      val actualTaxCodeAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(4).getElementsByTag("td").get(1).text()

      val actualScottishAnswer = parseHtml.getElementById("content").getElementsByTag("tr")
        .get(5).getElementsByTag("td").get(1).text()

      status shouldBe 200
      actualTableSize shouldBe expectedTableSize
      actualSalary shouldBe expectedHourlySalaryAnswer
      actualSalaryPeriod shouldBe expectedHourlyPeriodAnswer
      actualStatePensionAnswer shouldBe expectedStatePensionNO
      actualTaxCodeAnswer shouldBe expectedTaxCodeAnswer
      actualScottishAnswer shouldBe expectedScottishAnswer
    }
  }

}
