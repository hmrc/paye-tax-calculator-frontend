/*
 * Copyright 2026 HM Revenue & Customs
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

import forms.{LiveInScotlandFormProvider, StatePensionFormProvider}
import models.{QuickCalcAggregateInput, ScottishResident, StatePension}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, contentAsString, defaultAwaitTimeout, redirectLocation, route, running, status, writeableOf_AnyContentAsEmpty, writeableOf_AnyContentAsFormUrlEncoded}
import services.QuickCalcCache
import setup.QuickCalcCacheSetup.{aggregateCompleteListYearly, cacheSalaryStatePensionTaxCode, cacheScottishResident, cacheScottishResidentTest, cacheTaxCode, cacheTaxCodeStatePension, cacheTaxCodeStatePensionSalary}
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.{ScottishResidentView, StatePensionView}

import scala.concurrent.Future

class ScotlandResidentControllerSpec extends PlaySpec with TryValues with ScalaFutures with IntegrationPatience with MockitoSugar with CSRFTestHelper {

  val formProvider = new LiveInScotlandFormProvider()
  val form: Form[ScottishResident] = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(
                     app: Application,
                     lang: String = "en"
                   ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show live in scotland Form" should {

    def return200(lang: String = "en") = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(cacheScottishResidentTest))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      val formFilled = form.fill(
        cacheScottishResident.value.savedIsScottishResident.get
      )

      println("form filled :: "+formFilled)

      implicit val messages: Messages = messagesThing(application, lang)
      running(application) {

        val request = FakeRequest(GET, routes.ScotlandResidentController.showScottishResidentForm().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken
        val view = application.injector.instanceOf[ScottishResidentView]
        val result = route(application, request).value
        val doc: Document = Jsoup.parse(contentAsString(result))
        val header = doc.select(".govuk-header").text
        val betaBanner = doc.select(".govuk-phase-banner").text
        val heading = doc.select(".govuk-fieldset__heading").text
        val radios = doc.select(".govuk-radios__item")
        val details = doc.select(".govuk-details__summary-text").text()
        val detailComponent = doc.select(".govuk-details__text")
        val checkedRadios = doc.select(".govuk-radios__input[checked]")
        checkedRadios.attr("value") mustEqual "false"
        val button = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        status(result) mustEqual OK
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
        heading mustEqual messages("quick_calc.scottish_resident.header")
        button mustEqual messages("continue")
        radios.get(0).text mustEqual messages("quick_calc.scottish_resident.yes")
        radios.get(1).text mustEqual messages("quick_calc.scottish_resident.no")
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

    "return 303 and redirect to State pension page, with empty list of aggregate data" in {

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(
          GET,
          routes.ScotlandResidentController.showScottishResidentForm().url
        ).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm().url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }

    }
  }

  "Submit Scottish Resident Form" should {

    def return400(
                   fetchResponse: Option[QuickCalcAggregateInput] = None,
                   lang: String = "en"
                 ) = {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(fetchResponse))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, lang)

      running(application) {

        val request = FakeRequest(
          POST,
          routes.ScotlandResidentController.submitScottishResidentForm().url
        ).withFormUrlEncodedBody(form.data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader =
          parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual messages("error.summary.title")
        errorMessageLink.contains(messages("quick_calc.scottish_resident_error")) mustEqual true
        errorMessage.contains(messages("quick_calc.scottish_resident_error")) mustEqual true
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
          return400(cacheScottishResidentTest)
        }
        "form submitted in welsh" in {
          return400(cacheScottishResidentTest, lang = "cy")
        }
      }
    }

    "return 303 in English" when {

      def test300English(
                          cacheFetchData: Option[QuickCalcAggregateInput] = None,
                          cacheIsScottishResident: String
                        ): Unit = {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())).thenReturn(
          Future.successful(
            cacheFetchData
          )
        )
        when(mockCache.save(any())(any())).thenReturn(
          Future.successful(
            Done
          )
        )

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val formData = Map("overStatePensionAge" -> cacheIsScottishResident)

          val request = FakeRequest(
            GET,
            routes.ScotlandResidentController.showScottishResidentForm().url
          ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCSRFToken

          val result = route(application, request).value

          val view = application.injector.instanceOf[ScottishResidentView]

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm().url
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
