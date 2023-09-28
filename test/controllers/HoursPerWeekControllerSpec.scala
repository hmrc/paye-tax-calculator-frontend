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

import akka.Done
import forms.SalaryInHoursFormProvider
import models.Hours
import org.jsoup.Jsoup
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.HoursAWeekView
import play.api.test.CSRFTokenHelper._

import scala.concurrent.Future

class HoursPerWeekControllerSpec
    extends PlaySpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with CSRFTestHelper {
  val formProvider = new SalaryInHoursFormProvider()
  val form: Form[Hours] = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  def messagesForApp(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Hours Form" should {

    "return 200, with existing list of aggregate" in {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteHourly)

      implicit val messages: Messages = messagesForApp(application)

      val amount       = (cacheCompleteHourly.value.savedPeriod.value.amount * 100.0).toInt
      val howManyAWeek = cacheCompleteHourly.value.savedPeriod.value.howManyAWeek

      running(application) {

        val request = FakeRequest(GET, routes.HoursPerWeekController.showHoursAWeek(amount).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val view = application.injector.instanceOf[HoursAWeekView]

        val result = route(application, request).value

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(view(
          form.fill(Hours(cacheCompleteHourly.value.savedPeriod.value.amount, howManyAWeek)),
          cacheCompleteHourly.value.savedPeriod.value.amount
        )(request, messagesForApp(application)).toString)
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

        val request = FakeRequest(GET, routes.HoursPerWeekController.showHoursAWeek(0).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
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

        val request = FakeRequest(GET, routes.HoursPerWeekController.showHoursAWeek(0).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
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

        val request = FakeRequest(GET, routes.HoursPerWeekController.showHoursAWeek(0).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
      }
    }

  }
  "Submit Hours Form" should {

    "return 400 and error message when empty Hours Form submission" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedEmptyHoursErrorMessage) mustEqual true
        errorMessage.contains(expectedEmptyHoursErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400 and error message when Hours in a Week is 0" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "0")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedMinHoursAWeekErrorMessage) mustEqual true
        errorMessage.contains(expectedMinHoursAWeekErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400 and error message when Hours in a Week is 169" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "169")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedMaxHoursAWeekErrorMessage) mustEqual true
        errorMessage.contains(expectedMaxHoursAWeekErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400 and error message when Hours in a Week has more than 2 decimal places" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "37.555")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedWholeNumberHourlyErrorMessage) mustEqual true
        errorMessage.contains(expectedWholeNumberHourlyErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400 and error message when Hours in a Week is not numeric" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteHourly)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "test")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedWholeNumberHourlyErrorMessage) mustEqual true
        errorMessage.contains(expectedWholeNumberHourlyErrorMessage) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 303, with new Hours worked, 40.5 and complete aggregate" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteHourly)
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "40.5")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.YouHaveToldUsController.summary.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
        verify(mockCache, times(1)).save(any())(any())
      }
    }

    "return 303, with new Hours worked, 40.5 and incomplete aggregate" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePension
          .map(_.copy(savedIsOverStatePensionAge = None, savedTaxCode = None, savedScottishRate = None))
      )
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> "40.5")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.StatePensionController.showStatePensionForm.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
        verify(mockCache, times(1)).save(any())(any())
      }
    }
  }
}
