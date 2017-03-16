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

///*
// * Copyright 2017 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcControllerSpec
//
//import org.jsoup.Jsoup
//import play.api.test.Helpers._
//import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
//import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
//import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._
//
//class ShowResultSpec extends AppUnitGenerator {
//
//  "Show Result Page" should {
//    "return 200, with current list of aggregate which contains all answers from previous questions" in {
//      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
//      val action = csrfAddToken(controller.showResult())
//      val result = action.apply(request)
//      val status = result.header.status
//      val responseBody = contentAsString(result)
//      val parseHtml = Jsoup.parse(responseBody)
//
//      val expectedTaxCode = "1150L"
//      val expectedAgeAnswer = "YES"
//      val expectedSalary = "£20000"
//      val expectedSalaryType = "Per year"
//
//      val expectedNumberOfRows = 1 + aggregateListTaxCodeStatePensionAndSalary.size //Including header
//      val actualNumberOfRows = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").size()
//      val actualTaxCode = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(1).getElementsByTag("span").get(0).text()
//      val actualAgeAnswer = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(2).getElementsByTag("span").get(0).text()
//      val actualSalary = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(3).getElementsByTag("span").get(0).text()
//      val actualySalaryType = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(3).getElementsByTag("span").get(1).text()
//
//      status shouldBe 200
//      actualNumberOfRows shouldBe expectedNumberOfRows
//      actualTaxCode shouldBe expectedTaxCode
//      actualAgeAnswer shouldBe expectedAgeAnswer
//      actualSalary shouldBe expectedSalary
//      actualySalaryType shouldBe expectedSalaryType
//    }
//
//    "return 303, with current list of aggregate data and redirect to Tax Code Form if Tax Code is not provided" in {
//      val controller = new QuickCalcController(messages.messages, cacheReturnNoTaxCodeButAnswerEverythingElse)
//      val action = csrfAddToken(controller.showResult())
//      val result = action.apply(request)
//      val status = result.header.status
//
//      val actualRedirect = redirectLocation(result).get
//      val expectedRedirect = "/paye-tax-calculator/quick-calculation/tax-code"
//      status shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//
//    "return 303, with current list of aggregate data and redirect to Age Form if no answer is provided for \"Are you Over 65?\"" in {
//      val controller = new QuickCalcController(messages.messages, cacheReturnNoAgeButAnswerEverythingElse)
//      val action = csrfAddToken(controller.showResult())
//      val result = action.apply(request)
//      val status = result.header.status
//
//      val actualRedirect = redirectLocation(result).get
//      val expectedRedirect = "/paye-tax-calculator/quick-calculation/state_pension"
//      status shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//
//    "return 303, with current list of aggregate data and redirect to Salary Form if Salary is not provided" in {
//      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeAndIsOverStatePension)
//      val action = csrfAddToken(controller.showResult())
//      val result = action.apply(request)
//      val status = result.header.status
//
//      val actualRedirect = redirectLocation(result).get
//      val expectedRedirect = "/paye-tax-calculator/quick-calculation/salary"
//      status shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//
//    "return 303, with empty list of aggregate data and redirect to Tax Code Form" in {
//      val controller = new QuickCalcController(messages.messages, cacheEmpty)
//      val action = csrfAddToken(controller.showResult())
//      val result = action.apply(request)
//      val status = result.header.status
//
//      val expectedRedirect = "/paye-tax-calculator/quick-calculation/tax-code"
//      val actualRedirect = redirectLocation(result).get
//      status shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//  }
//
//}
