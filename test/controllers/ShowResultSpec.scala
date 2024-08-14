/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import config.features.Features
import forms.TaxResult
import org.jsoup.Jsoup
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, TryValues}
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import utils.DefaultTaxCodeProvider
import views.html.pages.ResultView

import scala.concurrent.Future

class ShowResultSpec extends BaseSpec with TryValues with IntegrationPatience with CSRFTestHelper with GuiceOneAppPerSuite with BeforeAndAfterEach {

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  private val features = new Features()(app.injector.instanceOf[Configuration])

  override def beforeEach(): Unit = {
    super.beforeEach()
    features.newScreenContentFeature(false)
    features.welshTranslationFeature(false)
  }

  def messagesThing(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Result Page" should {
    "return 200, with current list of aggregate which contains all answers from previous questions and sidebar links" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePensionSalary)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
        val taxResult =
          TaxResult.taxCalculation(cacheTaxCodeStatePensionSalary.get, defaultTaxCodeProvider)

        val request = FakeRequest(GET, routes.ShowResultsController.showResult.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[ResultView]

        status(result) mustEqual OK

        val responseBody = contentAsString(result)
        val parseHtml    = Jsoup.parse(responseBody)

        removeCSRFTagValue(responseBody) mustEqual removeCSRFTagValue(
          view(taxResult, defaultTaxCodeProvider.currentTaxYear, false, false, "2023/24", Seq.empty, pensionCheck = false, fourWeekly = false)(
            request,
            messagesThing(application)
          ).toString
        )

        val sidebar = parseHtml.getElementsByClass("govuk-grid-column-one-third")
        val links   = sidebar.get(0).getElementsByClass("govuk-link")

        links.size() mustEqual 3

        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }

    "return 200, and show disclaimer text if salary is over 100,002 and tax code is default uk or scottish" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheShowDisclaimer)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)
        val taxResult =
          TaxResult.taxCalculation(cacheShowDisclaimer.get, defaultTaxCodeProvider)

        val request = FakeRequest(GET, routes.ShowResultsController.showResult.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[ResultView]

        status(result) mustEqual OK

        val responseBody = contentAsString(result)
        val parseHtml    = Jsoup.parse(responseBody)

        removeCSRFTagValue(responseBody) mustEqual removeCSRFTagValue(
          view(taxResult, defaultTaxCodeProvider.currentTaxYear, false, true, "2023/24", Seq.empty, pensionCheck = false, fourWeekly = false)(
            request,
            messagesThing(application)
          ).toString
        )

        val sidebar        = parseHtml.getElementsByClass("govuk-grid-column-one-third")
        val links          = sidebar.get(0).getElementsByClass("govuk-link")
        val disclaimerText = parseHtml.getElementsByClass("govuk-warning-text__text").text()

        links.size() mustEqual 3
        disclaimerText.contains(disclaimerWarning) mustEqual true
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }

    "return 303, with current list of aggregate data and redirect to Tax Code Form if Tax Code is not provided" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeSalary)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.ShowResultsController.showResult.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.StatePensionController.showStatePensionForm.url
      }
    }

    "return 303, with current list of aggregate data and redirect to Salary Form if Salary is not provided" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.ShowResultsController.showResult.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.SalaryController.showSalaryForm.url
      }
    }

    "return 303, with current list of aggregate data and redirect to TaxCode Form Form if TaxCode is not provided" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePensionSalary.map(_.copy(savedTaxCode = None))
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.ShowResultsController.showResult.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.TaxCodeController.showTaxCodeForm.url
      }
    }
  }

  "return 200, with current list of aggregate data and isScottish is not provided" in {
    val mockCache = MockitoSugar.mock[QuickCalcCache]

    when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
      cacheTaxCodeStatePensionSalary.map(_.copy(savedScottishRate = None))
    )

    val application = new GuiceApplicationBuilder()
      .overrides(bind[QuickCalcCache].toInstance(mockCache))
      .build()

    running(application) {

      val request = FakeRequest(GET, routes.ShowResultsController.showResult.url)
        .withHeaders(HeaderNames.xSessionId -> "test")
        .withCSRFToken

      val result = route(application, request).get
      status(result) mustEqual OK
    }
  }
}
