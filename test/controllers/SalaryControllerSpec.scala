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

import forms.SalaryFormProvider
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
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
import views.html.pages.SalaryView

import scala.concurrent.Future

class SalaryControllerSpec
    extends PlaySpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with CSRFTestHelper {

  val formProvider = new SalaryFormProvider()
  val form         = formProvider()

  "Submit Salary Form" should {

    "return 400, with no aggregate data and empty Salary Form submission" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("amount" -> "", "period" -> "")

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400, with no aggregate data and empty Salary frequency Form submission" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("amount" -> "10", "period" -> "")

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedPayFrequencyErrorMessage) mustEqual true
        errorMessage.contains(expectedPayFrequencyErrorMessage) mustEqual true
      }
    }

    "return 400, with current list of aggregate data and an error message for empty Salary amount" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedEmptyErrorMessage) mustEqual true
        errorMessage.contains(expectedEmptyErrorMessage) mustEqual true
      }
    }

    "return 400 and error message when Salary submitted is \"0\" " in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "0", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedMinimumNumberErrorMessage) mustEqual true
        errorMessage.contains(expectedMinimumNumberErrorMessage) mustEqual true
      }
    }

    "return 400 and error message when Salary submitted is \"-1\" " in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "-1", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedMinimumNumberErrorMessage) mustEqual true
        errorMessage.contains(expectedMinimumNumberErrorMessage)
      }
    }

    "return 400 and error message when Salary submitted is more than 2 decimal places" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "23.3456547", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidSalaryErrorMessage) mustEqual true
        errorMessage.contains(expectedInvalidSalaryErrorMessage) mustEqual true

      }
    }

    "return 400 and error message when Salary submitted is \"10,000,000.00\"" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "100000000", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedMaxGrossPayErrorMessage) mustEqual true
        errorMessage.contains(expectedMaxGrossPayErrorMessage) mustEqual true
      }
    }

    "return 400 and error message when Salary submitted is not numeric" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "test", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(invalidInputErrorMessage) mustEqual true
        errorMessage.contains(invalidInputErrorMessage) mustEqual true
      }
    }

    """return 303, with new Yearly Salary "£20000", current list of aggregate data without State Pension Answer and redirect to State Pension Page""" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCode)
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "20000", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.StatePensionController.showStatePensionForm.url

        verify(mockCache, times(1)).save(any())(any())
      }
    }
    """return 303, with new Yearly Salary "£9,999,999.99", current list of aggregate data without State Pension Answer and redirect to State Pension Page""" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCode)
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "9999999.99", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.StatePensionController.showStatePensionForm.url

        verify(mockCache, times(1)).save(any())(any())
      }
    }

    """return 303, with new Yearly Salary data "£20000" and pound signs and commas included saved on a new list of aggregate data and redirect to State Pension Page""" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "£200,00", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.StatePensionController.showStatePensionForm.url

        verify(mockCache, times(1)).save(any())(any())
      }
    }

    """return 303, with new Yearly Salary data "£20000" saved on the complete list of aggregate data and redirect to State Pension Page""" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePensionSalary)
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "20000", "period" -> messages("quick_calc.salary.yearly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.YouHaveToldUsController.summary.url

        verify(mockCache, times(1)).save(any())(any())
      }
    }

    """return 303, with new Daily Salary data "£100" saved on the complete list of aggregate data and redirect to State Pension Page""" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteDaily.map(_.copy(savedIsOverStatePensionAge = None, savedScottishRate = None, savedTaxCode = None))
      )
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "100", "period" -> messages("quick_calc.salary.daily.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DaysPerWeekController
          .showDaysAWeek(10000)
          .url

        verify(mockCache, times(1)).save(any())(any())
      }
    }

    """return 303, with new Hourly Salary data "£100" saved on the complete list of aggregate data and redirect to State Pension Page""" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteHourly.map(_.copy(savedIsOverStatePensionAge = None))
      )
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> "100", "period" -> messages("quick_calc.salary.hourly.label"))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.HoursPerWeekController
          .showHoursAWeek(10000)
          .url

        verify(mockCache, times(1)).save(any())(any())
      }
    }

  }

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Salary Form" should {
    "return 200, with current list of aggregate data containing Tax Code: 1150L, \"YES\" for is not Over65, 20000 a Year for Salary" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePensionSalary)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      val formFilled = form.fill(cacheTaxCodeStatePensionSalary.value.savedSalary.get)
      running(application) {

        val request = FakeRequest(GET, routes.SalaryController.showSalaryForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        val view = application.injector.instanceOf[SalaryView]

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(formFilled)(request, messagesThing(application)).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }

    }

    "return 200, with empty list of aggregate data" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.SalaryController.showSalaryForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        val view = application.injector.instanceOf[SalaryView]

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual
        removeCSRFTagValue(view(form)(request, messagesThing(application)).toString)
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }
  }
}
