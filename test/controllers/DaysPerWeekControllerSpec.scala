/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.SalaryInDaysFormProvider
import models.Days
import org.jsoup.Jsoup
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.pages.DaysAWeekView

import scala.concurrent.Future

class DaysPerWeekControllerSpec
  extends PlaySpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {
  val formProvider = new SalaryInDaysFormProvider()
  val form: Form[Days] = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  def messagesForApp(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Days Form" should {

    "return 200, with existing list of aggregate" in {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteDaily)

      implicit val messages: Messages = messagesForApp(application)

      val amount       = cacheCompleteDaily.value.savedPeriod.value.amount

      running(application) {

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(amount, "").url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val view = application.injector.instanceOf[DaysAWeekView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, amount, "")(request, messagesForApp(application)).toString
      }
    }

    "return 303, redirect to start" in {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      implicit val messages: Messages = messagesForApp(application)

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      running(application) {

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(0, "").url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm().url
      }
    }

    "return 303, redirect to start if aggregate present but no savedSalary" in {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePension
          .map(_.copy(savedSalary = None))
      )

      implicit val messages: Messages = messagesForApp(application)

      running(application) {

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(0, "").url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm().url
      }
    }

    "return 303, redirect to start if aggregate returns None" in {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      implicit val messages: Messages = messagesForApp(application)

      running(application) {

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(0, "").url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm().url
      }
    }
  }
  "Submit Days Form" should {

    "return 400 and error message when empty Days Form submission" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "")

        val request = FakeRequest(POST, routes.DaysPerWeekController.submitDaysAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader  = parseHtml.getElementById("error-summary-title").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()

        errorHeader mustEqual "There is a problem"
        errorMessage.contains(expectedEmptyDaysErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400 and error message when Days in a Week is 0" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "0")

        val request = FakeRequest(POST, routes.DaysPerWeekController.submitDaysAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader  = parseHtml.getElementById("error-summary-title").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()

        errorHeader mustEqual "There is a problem"
        errorMessage.contains(expectedMinDaysAWeekErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400 and error message when Days in a Week is 8" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "8")

        val request = FakeRequest(POST, routes.DaysPerWeekController.submitDaysAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader  = parseHtml.getElementById("error-summary-title").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()

        errorHeader mustEqual "There is a problem"
        errorMessage.contains(expectedMaxDaysAWeekErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 303, with new Days worked, 5 and complete aggregate" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)
      when(mockCache.save(any())(any())) thenReturn Future.successful(CacheMap("id", Map.empty))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "5")

        val request = FakeRequest(POST, routes.DaysPerWeekController.submitDaysAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.QuickCalcController.summary().url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
        verify(mockCache, times(1)).save(any())(any())
      }
    }

    "return 303, with new Days worked, 5 and incomplete aggregate" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePension
          .map(_.copy(savedIsOverStatePensionAge = None, savedTaxCode = None, savedScottishRate = None))
      )
      when(mockCache.save(any())(any())) thenReturn Future.successful(CacheMap("id", Map.empty))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "5")

        val request = FakeRequest(POST, routes.DaysPerWeekController.submitDaysAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.QuickCalcController.showStatePensionForm().url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
        verify(mockCache, times(1)).save(any())(any())
      }
    }
  }
}
