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

package controllers

import forms.SalaryInDaysFormProvider
import models.{Days, QuickCalcAggregateInput}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import utils.BigDecimalFormatter
import views.html.pages.DaysAWeekView

import scala.concurrent.Future

class DaysPerWeekControllerSpec
    extends PlaySpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with CSRFTestHelper {
  val formProvider = new SalaryInDaysFormProvider()
  val form: Form[Days] = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesForApp(
    app:  Application,
    lang: String = "en"
  ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Days Form" should {

    def return200(lang: String = "en") = {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteDaily)

      val amount       = (cacheCompleteHourly.value.savedPeriod.value.amount * 100.0).toInt
      val howManyAweek = cacheCompleteDaily.value.savedPeriod.value.howManyAWeek

      val formFilled = form.fill(
        Days(cacheCompleteHourly.value.savedPeriod.value.amount,
             BigDecimalFormatter.stripZeros(howManyAweek.bigDecimal))
      )

      implicit val messages: Messages = messagesForApp(application, lang)

      running(application) {

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(amount).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val view = application.injector.instanceOf[DaysAWeekView]

        val result = route(application, request).value
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title   = doc.select(".govuk-label--xl").text
        val hint    = doc.select(".govuk-hint").text
        val button  = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        title must include(messages("quick_calc.salary.question.days_a_week"))
        hint mustEqual (messages("quick_calc.salary.question.approximate"))
        button mustEqual (messages("continue"))
        if (lang == "cy")
          deskpro.text()    must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        else deskpro.text() must include("Is this page not working properly? (opens in new tab)")

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(
            formFilled,
            cacheCompleteHourly.value.savedPeriod.value.amount
          )(request, messagesForApp(application, lang)).toString
        )
      }
    }

    "return 200, with existing list of aggregates" when {

      " form is loaded in english" in {
        return200()
      }

      " form is loaded in welsh" in {
        return200("cy")
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

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(0).url)
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

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(0).url)
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

        val request = FakeRequest(GET, routes.DaysPerWeekController.showDaysAWeek(0).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
      }
    }
  }
  "Submit Days Form" should {

    def return400(
      fetchResponse:      Option[QuickCalcAggregateInput],
      formWeeks:          String,
      errorMessageString: String,
      lang:               String = "en"
    ) = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application, lang)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> formWeeks)

        val request = FakeRequest(POST, routes.DaysPerWeekController.submitDaysAWeek(1).url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test-salary")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual messages("error.summary.title")
        errorMessageLink.contains(messages(errorMessageString)) mustEqual true
        errorMessage.contains(messages(errorMessageString)) mustEqual true
        verify(mockCache, times(0)).fetchAndGetEntry()(any())
      }
    }

    "return 400" when {
      "form is submitted empty" when {
        "language is in english" in {
          return400(cacheTaxCodeStatePension, "", "quick_calc.salary.question.error.empty_number_daily")
        }

        "language is in welsh" in {
          return400(cacheTaxCodeStatePension, "", "quick_calc.salary.question.error.empty_number_daily", "cy")
        }
      }

      "form is submitted with 0 in how many a week" when {
        "language is in english" in {
          return400(cacheTaxCodeStatePension, "0", "quick_calc.salary.question.error.number_of_days.invalid_hours")
        }

        "language is in welsh" in {
          return400(cacheTaxCodeStatePension,
                    "0",
                    "quick_calc.salary.question.error.number_of_days.invalid_hours",
                    "cy")
        }
      }

      "form is submitted with 8 in how many a week" when {
        "language is in english" in {
          return400(cacheTaxCodeStatePension, "8", "quick_calc.salary.question.error.number_of_days.invalid_hours")
        }

        "language is in welsh" in {
          return400(cacheTaxCodeStatePension,
                    "8",
                    "quick_calc.salary.question.error.number_of_days.invalid_hours",
                    "cy")
        }
      }

      "form is submitted with more than 2 decimal places in how many a week" when {
        "language is in english" in {
          return400(cacheTaxCodeStatePension, "3.555", "quick_calc.salary.question.error.number_of_days.invalid_number")
        }

        "language is in welsh" in {
          return400(cacheTaxCodeStatePension,
                    "3.555",
                    "quick_calc.salary.question.error.number_of_days.invalid_number",
                    "cy")
        }
      }

      "form is submitted with non numeric value  in how many a week" when {
        "language is in english" in {
          return400(cacheTaxCodeStatePension, "test", "quick_calc.salary.question.error.number_of_days.invalid_number")
        }

        "language is in welsh" in {
          return400(cacheTaxCodeStatePension,
                    "test",
                    "quick_calc.salary.question.error.number_of_days.invalid_number",
                    "cy")
        }
      }
    }
    "return 303" when {

      def return303(
        fetchResponse: Option[QuickCalcAggregateInput],
        redirectUrl:   String
      ) = {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)
        when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

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
          redirectLocation(result).value mustEqual redirectUrl
          verify(mockCache, times(1)).fetchAndGetEntry()(any())
          verify(mockCache, times(1)).save(any())(any())
        }
      }
      "new days worked , 5 and complete aggregate" in {
        return303(cacheCompleteDaily, routes.YouHaveToldUsNewController.summary.url)
      }

      "new days worked , 5 and incomplete aggregate" in {
        return303(
          cacheTaxCodeStatePension
            .map(_.copy(savedIsOverStatePensionAge = None, savedTaxCode = None, savedScottishRate = None)),
          routes.StatePensionController.showStatePensionForm.url
        )
      }
    }

  }
}
