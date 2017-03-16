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
//class ShowTaxCodeSpec extends AppUnitGenerator {
//
//  "Show Tax Code Form" should {
//    "return 200 and an empty list of aggregate data" in {
//
//      val controller = new QuickCalcController(messages.messages, cacheEmpty)
//      val action = csrfAddToken(controller.showTaxCodeForm())
//      val result = action.apply(request)
//      val status = result.header.status
//      val responseBody = contentAsString(result)
//      val parseHtml = Jsoup.parse(responseBody)
//
//      status shouldBe 200
//      parseHtml.getElementsByTag("tr").size shouldBe 0
//    }
//
//    "return 200 and a list of current aggregate data containing Tax Code: 1150L" in {
//      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
//      val action = csrfAddToken(controller.showTaxCodeForm())
//      val result = action.apply(request)
//      val status = result.header.status
//      val responseBody = contentAsString(result)
//      val parseHtml = Jsoup.parse(responseBody)
//
//      val expectedNumberOfRows = 1 + aggregateListOnlyTaxCode.size //Including header
//      val actualTaxCode = parseHtml.getElementsByTag("tr").get(1).getElementsByTag("span").get(0).text()
//      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
//
//      status shouldBe 200
//      actualNumberOfRows shouldBe expectedNumberOfRows
//      actualTaxCode shouldBe expectedTaxCode
//    }
//  }
//
//}
