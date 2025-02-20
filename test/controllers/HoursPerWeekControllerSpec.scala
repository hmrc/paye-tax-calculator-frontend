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

import forms.SalaryInHoursFormProvider
import models.{Hours, QuickCalcAggregateInput}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.HoursAWeekView
import play.api.test.CSRFTokenHelper._
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

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

  def messagesForApp(
    app:  Application,
    lang: String = "en"
  ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)
  "Show Hours Form" should {

    def return200(lang: String = "en") = {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheCompleteHourly)

      implicit val messages: Messages = messagesForApp(application, lang)

      val amount       = (cacheCompleteHourly.value.savedPeriod.value.amount * 100.0).toInt
      val howManyAWeek = cacheCompleteHourly.value.savedPeriod.value.howManyAWeek

      running(application) {

        val request = FakeRequest(GET, routes.HoursPerWeekController.showHoursAWeek(amount).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val view = application.injector.instanceOf[HoursAWeekView]

        val result = route(application, request).value
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title   = doc.select(".govuk-label--xl").text
        val hint    = doc.select(".govuk-hint").text
        val button  = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        status(result) mustEqual OK
        title must include(messages("quick_calc.salary.question.hours_a_week"))
        hint mustEqual (messages("quick_calc.salary.question.approximate"))
        button mustEqual (messages("continue"))
        if (lang == "cy")
          deskpro.text()    must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        else deskpro.text() must include("Is this page not working properly? (opens in new tab)")

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(
            form.fill(Hours(cacheCompleteHourly.value.savedPeriod.value.amount, howManyAWeek)),
            cacheCompleteHourly.value.savedPeriod.value.amount
          )(request, messagesForApp(application, lang)).toString
        )
      }
    }

    "return 200 " when {
      "with existing list of aggregate in English" in {
        return200()
      }
      "with existing list of aggregate in Welsh" in {
        return200("cy")
      }
    }

    def return300(
      redirectUrl:   String,
      fetchResponse: Option[QuickCalcAggregateInput]
    ) = {
      val mockCache = mock[QuickCalcCache]

      val application: Application = new GuiceApplicationBuilder()
        .overrides(
          bind[QuickCalcCache].toInstance(mockCache)
        )
        .build()

      implicit val messages: Messages = messagesForApp(application)

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

      running(application) {

        val request = FakeRequest(GET, routes.HoursPerWeekController.showHoursAWeek(0).url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual redirectUrl
      }

    }

    "return 303, redirect to start" when {

      "no aggregate data is there, redirect to start" in {
        return300(routes.SalaryController.showSalaryForm.url, None)
      }

      "aggregate present but no savedSalary, redirect to start" in {
        return300(routes.SalaryController.showSalaryForm.url,
                  cacheTaxCodeStatePension
                    .map(_.copy(savedSalary = None)))
      }
    }

  }
  "Submit Hours Form" should {

    def return400(
      hrsPerWeek:         String,
      errorMessageString: String,
      lang:               String = "en"
    ) = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePension)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesForApp(application, lang)
      running(application) {
        val formData = Map("amount" -> "1", "how-many-a-week" -> hrsPerWeek)

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
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

    "return 400 and error message  " when {
      "No hrs entered in form " when {
        " form is submitted in english" in {
          return400("", "quick_calc.salary.question.error.empty_number_hourly")
        }
        " form is submitted in welsh" in {
          return400("", "quick_calc.salary.question.error.empty_number_hourly", "cy")
        }
      }

      "Hours in a Week value is entered as 0" when {
        " form is submitted in english" in {
          return400("0", "quick_calc.salary.question.error.number_of_hours.invalid_number")
        }
        " form is submitted in welsh" in {
          return400("0", "quick_calc.salary.question.error.number_of_hours.invalid_number", "cy")
        }
      }

      "Hours in a Week value is entered as 169" when {
        " form is submitted in english" in {
          return400("169", "quick_calc.salary.question.error.number_of_hours.invalid_number")
        }
        " form is submitted in welsh" in {
          return400("169", "quick_calc.salary.question.error.number_of_hours.invalid_number", "cy")
        }
      }

      "Hours in a Week value is entered as more than 2 decimal places" when {
        " form is submitted in english" in {
          return400("37.555", "quick_calc.salary.question.error.invalid_number_hourly")
        }
        " form is submitted in welsh" in {
          return400("37.555", "quick_calc.salary.question.error.invalid_number_hourly", "cy")
        }
      }

      "Hours in a Week value is entered as non numeric" when {
        " form is submitted in english" in {
          return400("test", "quick_calc.salary.question.error.invalid_number_hourly")
        }
        " form is submitted in welsh" in {
          return400("test", "quick_calc.salary.question.error.invalid_number_hourly", "cy")
        }
      }
    }

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
        val formData = Map("amount" -> "1", "how-many-a-week" -> "40.5")

        val request = FakeRequest(POST, routes.HoursPerWeekController.submitHoursAWeek(1).url)
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

    "return 303" when {
      "New Hours worked, 40.5 and complete aggregate, redirect to you have told us page" in {
        return303(cacheCompleteHourly, routes.YouHaveToldUsNewController.summary.url)
      }

      "New Hours worked, 40.5 and incomplete aggregate, redirect show state pension page" in {
        return303(
          cacheTaxCodeStatePension
            .map(_.copy(savedIsOverStatePensionAge = None, savedTaxCode = None, savedScottishRate = None)),
          routes.StatePensionController.showStatePensionForm.url
        )
      }
    }
  }
}
