/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.UserTaxCodeFormProvider
import org.jsoup.Jsoup
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
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
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.DefaultTaxCodeProvider
import views.html.pages.TaxCodeView

import scala.concurrent.Future

class TaxCodeControllerSpec
    extends BaseSpec
    with AnyWordSpecLike
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
      with CSRFTestHelper {
  val formProvider = new UserTaxCodeFormProvider()
  val form         = formProvider()
  val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider((appConfig))

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Tax Code Form" should {
    "return 200 and an empty list of aggregate data" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.TaxCodeController.showTaxCodeForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.SalaryController.showSalaryForm.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }

    "return 200 and a list of current aggregate data containing Tax Code and pension" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePensionSalary.map(_.copy(savedTaxCode = None))
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.TaxCodeController.showTaxCodeForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[TaxCodeView]

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(view(
          form,
          cacheTaxCodeStatePensionSalary.map(_.copy(savedTaxCode = None)).get.youHaveToldUsItems,
          defaultTaxCodeProvider.defaultUkTaxCode
        )(
          request,
          messagesThing(application)
        ).toString)
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }

    "return 200 and a list of current aggregate data containing Tax Code and pension and tax code answered" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheSalaryStatePensionTaxCode)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.TaxCodeController.showTaxCodeForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[TaxCodeView]

        val formFilled = form.fill(cacheSalaryStatePensionTaxCode.value.savedTaxCode.get)

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(view(formFilled,
                                               cacheSalaryStatePensionTaxCode.value.youHaveToldUsItems,
                                               defaultTaxCodeProvider.defaultUkTaxCode)(
          request,
          messagesThing(application)
        ).toString)
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }
  }

  "Submit Tax Code Form" should {
    "return 400, with aggregate data and an error message for invalid Tax Code" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "110")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementById("error-summary-title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidTaxCodeErrorMessage) mustEqual true
        errorMessage.contains(expectedInvalidTaxCodeErrorMessage) mustEqual true
      }
    }

    "return 400, with no aggregate data and an error message for invalid Tax Code" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "110")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementById("error-summary-title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidTaxCodeErrorMessage) mustEqual true
        errorMessage.contains(expectedInvalidTaxCodeErrorMessage) mustEqual true
      }
    }

    "return 400, with no aggregate data and an error message for invalid Tax Code Prefix" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "X9999")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementById("error-summary-title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"

        errorMessageLink.contains(expectedPrefixTaxCodeErrorMessage) mustEqual true
        errorMessage.contains(expectedPrefixTaxCodeErrorMessage) mustEqual true
      }
    }

    "return 400, with no aggregate data and an error message for invalid Tax Code Suffix" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "9999A")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementById("error-summary-title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"

        errorMessageLink.contains(expectedSuffixTaxCodeErrorMessage) mustEqual true
        errorMessage.contains(expectedSuffixTaxCodeErrorMessage) mustEqual true
      }
    }

    "return 400, with no aggregate data and an error message when Tax Code entered is 99999L" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "99999L")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementById("error-summary-title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"

        errorMessageLink.contains(expectedInvalidTaxCodeErrorMessage) mustEqual true
        errorMessage.contains(expectedInvalidTaxCodeErrorMessage) mustEqual true
      }
    }

    "return 303, with current aggregate data and redirect to Is Over State Pension Page" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)
      when(mockCache.save(any())(any())) thenReturn Future.successful(CacheMap("id", Map.empty))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {
        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "K425")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.YouHaveToldUsController.summary.url
      }
    }

    "return 303, with no aggregate data and redirect to Is Over State Pension Page" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)
      when(mockCache.save(any())(any())) thenReturn Future.successful(CacheMap("id", Map.empty))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {
        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "K425")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.YouHaveToldUsController.summary.url
      }
    }

    "return 303, with current aggregate data and redirect to Summary Result Page" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePensionSalary)
      when(mockCache.save(any())(any())) thenReturn Future.successful(CacheMap("id", Map.empty))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {
        val formData = Map("hasTaxCode" -> "true", "taxCode" -> "K425")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.YouHaveToldUsController.summary.url

      }
    }

    "return 303, with to scottish page if No to tax code" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePensionSalary.map(_.copy(savedTaxCode = None))
      )
      when(mockCache.save(any())(any())) thenReturn Future.successful(CacheMap("id", Map.empty))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {
        val formData = Map("hasTaxCode" -> "false")

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ScottishRateController.showScottishRateForm.url
      }
    }
  }

}
