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

import forms.SalaryFormProvider
import models.QuickCalcAggregateInput
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
import play.api.mvc.{AnyContentAsEmpty, Cookie, RequestHeader, Result}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.SalaryView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class SalaryControllerSpec
    extends PlaySpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with CSRFTestHelper {

  val formProvider = new SalaryFormProvider()
  val form         = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(
    app:  Application,
    lang: String = "en"
  ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Salary Form" should {

    def return200(
      fetchResponse: Option[QuickCalcAggregateInput],
      isFormFill:    Boolean,
      lang:          String = "en"
    ) = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, lang)

      val formFilled =
        if (isFormFill) form.fill(cacheTaxCodeStatePensionSalary.value.savedSalary.get)
        else
          form

      running(application) {

        val request = FakeRequest(GET, routes.SalaryController.showSalaryForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).value
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title   = doc.select("title").text
        val label   = doc.select(".govuk-label").text
        val hint    = doc.select(".govuk-hint").text
        val heading = doc.select(".govuk-fieldset__heading").text
        val radios  = doc.select(".govuk-radios__item")
        val button  = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        val radioSelected = if (isFormFill) doc.select(".govuk-radios__input[checked]").attr("value")

        val view = application.injector.instanceOf[SalaryView]

        status(result) mustEqual OK
        title must include(messages("quick_calc.salary.header"))
        label must include(messages("quick_calc.salary.grossAmount.label"))
        hint mustEqual messages("hint.salary")
        heading mustEqual messages("label.payperiod")
        button mustEqual messages("continue")
        radios.get(0).text mustEqual messages("quick_calc.salary.yearly.text")
        radios.get(1).text mustEqual messages("quick_calc.salary.monthly.text")
        radios.get(2).text mustEqual messages("quick_calc.salary.fourWeeks.text")
        radios.get(3).text mustEqual messages("quick_calc.salary.weekly.text")
        radios.get(4).text mustEqual messages("quick_calc.salary.daily.text")
        radios.get(5).text mustEqual messages("quick_calc.salary.hourly.text")
        if (lang == "cy")
          deskpro.text()    must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        else deskpro.text() must include("Is this page not working properly? (opens in new tab)")
        if (isFormFill) radioSelected mustEqual "a year"

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(formFilled)(request, messagesThing(application, lang)).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }

    }

    "return 200 " when {
      "with current list of aggregate data containing Tax Code: 1150L, \"YES\" for is not Over65, 20000 a Year for Salary" when {
        " form submitted in english" in {
          return200(cacheTaxCodeStatePensionSalary, true)
        }

        " form submitted in Welsh" in {
          return200(cacheTaxCodeStatePensionSalary, true, "cy")
        }
      }

      "with with empty list of aggregate data" when {
        " form submitted in english" in {
          return200(None, false)
        }

        " form submitted in Welsh" in {
          return200(None, false, "cy")
        }
      }
    }
  }

  "Submit Salary Form" should {

    def return400(
      formData:           Map[String, String],
      errorMessageString: String,
      fetchResponse:      Option[QuickCalcAggregateInput] = None,
      lang:               String = "en"
    ) = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, lang)

      running(application) {

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        verify(mockCache, times(0)).fetchAndGetEntry()(any())
        val parseHtml        = Jsoup.parse(contentAsString(result))
        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual messages("error.summary.title")
        errorMessageLink.contains(messages(errorMessageString)) mustEqual true
        errorMessage.contains(messages(errorMessageString)) mustEqual true
      }
    }

    "return 400 and error message " when {

      "no aggregate data and empty form" when {
        "submitted in english" in {
          return400(Map("amount" -> "", "period" -> ""), "quick_calc.salary.question.error.empty_salary_input")
          return400(Map("amount" -> "", "period" -> ""), "quick_calc.salary.option_error")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "", "period" -> ""),
                    "quick_calc.salary.question.error.empty_salary_input",
                    lang = "cy")
          return400(Map("amount" -> "", "period" -> ""), "quick_calc.salary.option_error", lang = "cy")
        }
      }

      "no aggregate data and no frequency selected in the form" when {
        "submitted in english" in {
          return400(Map("amount" -> "10", "period" -> ""), "quick_calc.salary.option_error")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "10", "period" -> ""), "quick_calc.salary.option_error", lang = "cy")

        }
      }

      "current list of aggregate data and no amount filled in the form" when {
        "submitted in english" in {
          return400(Map("amount" -> "", "period" -> "a year"), "quick_calc.salary.question.error.empty_salary_input")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "", "period" -> "a year"),
                    "quick_calc.salary.question.error.empty_salary_input",
                    lang = "cy")

        }
      }

      "No aggregate data and Salary submitted is \"0\"" when {
        "submitted in english" in {
          return400(Map("amount" -> "0", "period" -> "a year"), "quick_calc.salary.question.error.minimum_salary_input")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "0", "period" -> "a year"),
                    "quick_calc.salary.question.error.minimum_salary_input",
                    lang = "cy")

        }
      }

      "No aggregate data and Salary submitted is \"-1\"" when {
        "submitted in english" in {
          return400(Map("amount" -> "-1", "period" -> "a year"),
                    "quick_calc.salary.question.error.minimum_salary_input")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "-1", "period" -> "a year"),
                    "quick_calc.salary.question.error.minimum_salary_input",
                    lang = "cy")

        }
      }

      "No aggregate data and Salary submitted is more than 2 decimal places" when {
        "submitted in english" in {
          return400(Map("amount" -> "23.3456547", "period" -> "a year"),
                    "quick_calc.salary.question.error.invalid_salary")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "23.3456547", "period" -> "a year"),
                    "quick_calc.salary.question.error.invalid_salary",
                    lang = "cy")

        }
      }

      "No aggregate data and Salary submitted is \"10,000,000.00\"" when {
        "submitted in english" in {
          return400(Map("amount" -> "100000000", "period" -> "a year"),
                    "quick_calc.salary.question.error.maximum_salary_input")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "100000000", "period" -> "a year"),
                    "quick_calc.salary.question.error.maximum_salary_input",
                    lang = "cy")

        }
      }

      "No aggregate data and Salary submitted is not numeric" when {
        "submitted in english" in {
          return400(Map("amount" -> "test", "period" -> "a year"), "quick_calc.salary.question.error.invalid_salary")
        }

        "submitted in welsh" in {
          return400(Map("amount" -> "test", "period" -> "a year"),
                    "quick_calc.salary.question.error.invalid_salary",
                    lang = "cy")

        }
      }
    }

    def return303(
      fetchResponse: Option[QuickCalcAggregateInput],
      amount:        String,
      redirectUrl:   String,
      frequency:     String = "quick_calc.salary.yearly.label"
    ) = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("amount" -> amount, "period" -> messages(frequency))

        val request = FakeRequest(POST, routes.SalaryController.submitSalaryAmount.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual redirectUrl

        verify(mockCache, times(1)).save(any())(any())
      }
    }

    "return 303" when {

      "current list of aggregate data, salary- £20000 without State Pension  and redirect to State Pension Page" in {
        return303(cacheTaxCode, "20000", routes.StatePensionController.showStatePensionForm.url)
      }

      "current list of aggregate data, salary- £9,999,999.99 without State Pension  and redirect to State Pension Page" in {
        return303(cacheTaxCode, "9,999,999.99", routes.StatePensionController.showStatePensionForm.url)
      }

      "No aggregate data, salary- £20000 with pound sign and commas , redirect to State Pension Page" in {
        return303(None, "£200,00", routes.StatePensionController.showStatePensionForm.url)
      }

      "Aggregate data, salary- £20000  redirect to Days per week  Page" in {
        return303(cacheTaxCodeStatePensionSalary, "20000", routes.YouHaveToldUsNewController.summary.url)
      }

      "Aggregate data, new Daily Salary data £100  redirect to State Pension Page" in {
        return303(
          cacheCompleteDaily.map(
            _.copy(savedIsOverStatePensionAge = None, savedScottishRate = None, savedTaxCode = None)
          ),
          "100",
          routes.DaysPerWeekController.showDaysAWeek(10000).url,
          "quick_calc.salary.daily.label"
        )
      }

      "Aggregate data, new Hourly Salary data £100  redirect to Hours per week Page" in {
        return303(
          cacheCompleteHourly.map(_.copy(savedIsOverStatePensionAge = None)),
          "100",
          routes.HoursPerWeekController.showHoursAWeek(10000).url,
          "quick_calc.salary.hourly.label"
        )
      }
    }
  }
}
