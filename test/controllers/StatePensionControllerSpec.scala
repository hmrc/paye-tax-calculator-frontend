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

import forms.StatePensionFormProvider
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
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.StatePensionView

import scala.concurrent.Future

class StatePensionControllerSpec
    extends PlaySpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with CSRFTestHelper {

  val formProvider = new StatePensionFormProvider()
  val form         = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(
    app:  Application,
    lang: String = "en"
  ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show State Pension Form" should {

    def return200(lang: String = "en") = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheSalaryStatePensionTaxCode)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      val formFilled = form.fill(
        cacheTaxCodeStatePensionSalary.value.savedIsOverStatePensionAge.get
      )

      implicit val messages: Messages = messagesThing(application, lang)
      running(application) {

        val request = FakeRequest(GET, routes.StatePensionController.showStatePensionForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken
        val view   = application.injector.instanceOf[StatePensionView]
        val result = route(application, request).value
        val doc: Document = Jsoup.parse(contentAsString(result))
        val header          = doc.select(".govuk-header").text
        val betaBanner      = doc.select(".govuk-phase-banner").text
        val heading         = doc.select(".govuk-fieldset__heading").text
        val radios          = doc.select(".govuk-radios__item")
        val details         = doc.select(".govuk-details__summary-text").text()
        val detailComponent = doc.select(".govuk-details__text")
        val checkedRadios   = doc.select(".govuk-radios__input[checked]")
        checkedRadios.attr("value") mustEqual ("true")
        val button  = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        status(result) mustEqual (OK)
        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(
            formFilled,
            aggregateCompleteListYearly
          )(request, messagesThing(application, lang)).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

        header     must include(messages("quick_calc.header.title"))
        betaBanner must include(messages("feedback.before"))
        betaBanner must include(messages("feedback.link"))
        betaBanner must include(messages("feedback.after"))
        heading mustEqual (messages("quick_calc.you_have_told_us.over_state_pension_age.label"))
        button mustEqual (messages("continue"))
        radios.get(0).text mustEqual (messages("quick_calc.you_have_told_us.over_state_pension_age.yes"))
        radios.get(1).text mustEqual (messages("quick_calc.you_have_told_us.over_state_pension_age.no"))
        details mustEqual (messages("label.state-pension-details"))
        detailComponent.text must include(messages("quick_calc.salary.question.state_pension_info"))
        detailComponent.text must include(messages("quick_calc.salary.question.state_pension_url_a"))
        detailComponent.text must include(messages("quick_calc.salary.question.state_pension_url_b"))
        detailComponent.text must include(messages("quick_calc.salary.question.state_pension_url_c"))
        if (lang == "cy")
          deskpro.text()    must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        else deskpro.text() must include("Is this page not working properly? (opens in new tab)")

      }
    }

    "return 200" when {
      "existing aggregate data in form" when {
        "submitted in english" in {
          return200()
        }

        "submitted in welsh" in {
          return200("cy")
        }
      }
    }

    "return 303 and redirect to Salary page, with empty list of aggregate data" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        None
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(
          GET,
          routes.StatePensionController.showStatePensionForm.url
        ).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }

    }
  }

  "Submit State Pension Form" should {

    def return400(
      fetchResponse: Option[QuickCalcAggregateInput] = None,
      lang:          String                          = "en"
    ) = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, lang)

      running(application) {

        val request = FakeRequest(
          POST,
          routes.StatePensionController.submitStatePensionForm.url
        ).withFormUrlEncodedBody(form.data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader =
          parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual messages("error.summary.title")
        errorMessageLink.contains(messages("quick_calc.over_state_pension_age_error")) mustEqual true
        errorMessage.contains(messages("quick_calc.over_state_pension_age_error")) mustEqual true
      }
    }

    "return 400 with invalid form answer" when {
      "empty list of aggregate data is there" when {
        "form submitted in english" in {
          return400()
        }
        "form submitted in welsh" in {
          return400(lang = "cy")
        }
      }

      "list of aggregate data is there" when {
        "form submitted in english" in {
          return400(cacheTaxCodeStatePension)
        }
        "form submitted in welsh" in {
          return400(cacheTaxCodeStatePension, lang = "cy")
        }
      }
    }

    "return 303 in English" when {

      def test300English(
        cacheFetchData:       Option[QuickCalcAggregateInput] = None,
        checkStatePensionAge: String
      ): Unit = {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
          cacheFetchData
        )
        when(mockCache.save(any())(any())) thenReturn Future.successful(
          Done
        )

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val formData = Map("overStatePensionAge" -> checkStatePensionAge)

          val request = FakeRequest(
            GET,
            routes.StatePensionController.showStatePensionForm.url
          ).withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCSRFToken

          val result = route(application, request).value

          val view = application.injector.instanceOf[StatePensionView]

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
          verify(mockCache, times(1)).fetchAndGetEntry()(any())
        }
      }
      "When answer \"No\" saved on existing list of aggregate data without Salary and redirect to Salary Page" in {
        test300English(cacheTaxCode, "false")
      }
      "When answer \"Yes\" for being Over 65 saved on a new list of aggregate data and redirect Salary Page" in {
        test300English(cacheTaxCode, "true")
      }
    }

  }

}
