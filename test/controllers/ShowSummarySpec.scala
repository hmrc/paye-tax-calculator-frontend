/*
 * Copyright 2024 HM Revenue & Customs
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
// * Copyright 2023 HM Revenue & Customs
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
//package controllers
//
//import org.jsoup.Jsoup
//import org.mockito.Mockito.when
//import org.mockito.ArgumentMatchers.any
//import org.scalatest.TryValues
//import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
//import org.scalatestplus.mockito.MockitoSugar
//import org.scalatestplus.play.PlaySpec
//import play.api.inject.bind
//import play.api.inject.guice.GuiceApplicationBuilder
//import play.api.test.FakeRequest
//import play.api.test.Helpers._
//import services.QuickCalcCache
//import setup.QuickCalcCacheSetup._
//import uk.gov.hmrc.http.HeaderNames
//import play.api.test.CSRFTokenHelper._
//
//import scala.concurrent.Future
//
//class ShowSummarySpec extends PlaySpec with TryValues with ScalaFutures with IntegrationPatience with MockitoSugar {
//
//  "Show Summary Page" should {
//
//    "return aggregate data of : Earning £20000 Yearly Salary, NOT (Over State Pension), Tax Code: S1150L and IS Scottish Tax Payer" in {
//      val mockCache = mock[QuickCalcCache]
//
//      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteYearlyScottish)
//      val application = new GuiceApplicationBuilder()
//        .overrides(bind[QuickCalcCache].toInstance(mockCache))
//        .build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary.url)
//          .withHeaders(HeaderNames.xSessionId -> "test")
//          .withCSRFToken
//
//        val result = route(application, request).value
//
//        status(result) mustBe OK
//
//        val responseBody = contentAsString(result)
//        val parseHtml    = Jsoup.parse(responseBody)
//
//        val actualTable = parseHtml.getElementsByClass("govuk-summary-list__row")
//        actualTable.size() mustBe 4
//
//        actualTable
//          .get(0)
//          .getElementsByClass("govuk-summary-list__value")
//          .get(0)
//          .text() mustBe expectedYearlySalaryAnswer
//        actualTable.get(1).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedStatePensionNO
//        actualTable
//          .get(2)
//          .getElementsByClass("govuk-summary-list__value")
//          .get(0)
//          .text() mustBe expectedTaxCodeAnswerScottish
//        actualTable
//          .get(3)
//          .getElementsByClass("govuk-summary-list__value")
//          .get(0)
//          .text() mustBe expectedScottishAnswerYes
//      }
//    }
//
//    "return aggregate data of : Earning £40 Daily Salary, 5 Days a Week, NOT (Over State Pension), Tax Code: 1150L and is NOT Scottish Tax Payer" in {
//      val mockCache = mock[QuickCalcCache]
//
//      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteDaily)
//      val application = new GuiceApplicationBuilder()
//        .overrides(bind[QuickCalcCache].toInstance(mockCache))
//        .build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary.url)
//          .withHeaders(HeaderNames.xSessionId -> "test")
//          .withCSRFToken
//
//        val result = route(application, request).value
//
//        status(result) mustBe OK
//
//        val responseBody = contentAsString(result)
//        val parseHtml    = Jsoup.parse(responseBody)
//
//        val actualTable = parseHtml.getElementsByClass("govuk-summary-list__row")
//        actualTable.size() mustBe 5
//
//        actualTable
//          .get(0)
//          .getElementsByClass("govuk-summary-list__value")
//          .get(0)
//          .text() mustBe expectedDailySalaryAnswer
//        actualTable
//          .get(1)
//          .getElementsByClass("govuk-summary-list__value")
//          .get(0)
//          .text() mustBe expectedDailyPeriodAnswer
//        actualTable.get(2).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedStatePensionNO
//        actualTable.get(3).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedTaxCodeAnswer
//        actualTable.get(4).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedScottishAnswer
//      }
//    }
//    "return aggregate data of : Earning £8.5 Hourly Salary, YES (Over State Pension), Tax Code: 1150L and is NOT Scottish Tax Payer" in {
//      val mockCache = mock[QuickCalcCache]
//
//      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteHourly)
//      val application = new GuiceApplicationBuilder()
//        .overrides(bind[QuickCalcCache].toInstance(mockCache))
//        .build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary.url)
//          .withHeaders(HeaderNames.xSessionId -> "test")
//          .withCSRFToken
//
//        val result = route(application, request).value
//
//        status(result) mustBe OK
//
//        val responseBody = contentAsString(result)
//        val parseHtml    = Jsoup.parse(responseBody)
//
//        val actualTable = parseHtml.getElementsByClass("govuk-summary-list__row")
//        actualTable.size() mustBe 5
//
//        actualTable
//          .get(0)
//          .getElementsByClass("govuk-summary-list__value")
//          .get(0)
//          .text() mustBe expectedHourlySalaryAnswer
//        actualTable
//          .get(1)
//          .getElementsByClass("govuk-summary-list__value")
//          .get(0)
//          .text() mustBe expectedHourlyPeriodAnswer
//        actualTable.get(2).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedStatePensionNO
//        actualTable.get(3).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedTaxCodeAnswer
//        actualTable.get(4).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedScottishAnswer
//      }
//    }
//  }
//}
