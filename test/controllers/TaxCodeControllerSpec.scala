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

import forms.{UserTaxCodeFormProvider, YouHaveToldUsItem}
import models.{QuickCalcAggregateInput, UserTaxCode}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
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
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
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
  val defaultTaxCodeProvider: DefaultTaxCodeProvider = new DefaultTaxCodeProvider(mockAppConfig)

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

  "Show Tax Code Form" should {

    def return200(
      fetchResponse:  Option[QuickCalcAggregateInput],
      formFilled:     Form[UserTaxCode],
      aggregateInput: QuickCalcAggregateInput,
      lang:           String = "en"
    ) = {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, lang)

      running(application) {

        val request = FakeRequest(GET, routes.TaxCodeController.showTaxCodeForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title   = doc.select(".govuk-heading-xl").text
        val hint    = doc.select(".govuk-hint").text
        val summary = doc.select(".govuk-details__summary-text").text
        val text1   = doc.select(".govuk-details__text").text
        val bullets = doc.select(".govuk-list--bullet")
        val button  = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        val view = application.injector.instanceOf[TaxCodeView]

        status(result) mustEqual OK

        title must include(messages("quick_calc.about_tax_code.header"))
        hint  must include(messages("quick_calc.about_tax_code.details.firstInfoPara", "1257L"))
        summary mustEqual (messages("label.tax-code.new"))
        text1        must include(messages("quick_calc.about_tax_code.details.start"))
        bullets.html must include(messages("quick_calc.about_tax_code.details.firstBullet_b"))
        bullets.html must include(messages("quick_calc.about_tax_code.details.secondBullet"))
        bullets.html must include(messages("quick_calc.about_tax_code.details.thirdBullet"))
        bullets.html must include(messages("quick_calc.about_tax_code.details.fourthBullet"))
        button mustEqual (messages("continue"))
        if (lang == "cy")
          deskpro.text()    must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        else deskpro.text() must include("Is this page not working properly? (opens in new tab)")

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(
            formFilled,
            aggregateInput
              .youHaveToldUsItems()(messages, mockAppConfig),
            defaultTaxCodeProvider.defaultUkTaxCode,
            false
          )(
            request,
            messagesThing(application, lang)
          ).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }

    "return 303 and an empty list of aggregate data" in {

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

    "return 200" when {
      "list of current aggregate data containing tax code and  pension" when {
        "form submitted in english" in {
          return200(cacheTaxCodeStatePensionSalary.map(_.copy(savedTaxCode = None)),
                    form,
                    cacheTaxCodeStatePensionSalary
                      .map(_.copy(savedTaxCode = None))
                      .get)

        }

        "form submitted in welsh" in {
          return200(cacheTaxCodeStatePensionSalary.map(_.copy(savedTaxCode = None)),
                    form,
                    cacheTaxCodeStatePensionSalary
                      .map(_.copy(savedTaxCode = None))
                      .get,
                    "cy")

        }
      }

      "list of current aggregate data containing tax code and pension and tax code answered" when {
        "form submitted in english" in {
          return200(cacheSalaryStatePensionTaxCode,
                    form.fill(cacheSalaryStatePensionTaxCode.get.savedTaxCode.get),
                    cacheSalaryStatePensionTaxCode.get)

        }

        "form submitted in welsh" in {
          return200(cacheSalaryStatePensionTaxCode,
                    form.fill(cacheSalaryStatePensionTaxCode.get.savedTaxCode.get),
                    cacheSalaryStatePensionTaxCode.get,
                    "cy")

        }
      }
    }
  }

  "Submit Tax Code Form" should {

    def return400(
      errorMsg:       String,
      taxCode:        String,
      cacheFetchData: Option[QuickCalcAggregateInput] = None,
      lang:           String = "en"
    ) = {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheFetchData)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application, lang)
      running(application) {

        val formData = Map("hasTaxCode" -> "true", "taxCode" -> taxCode)

        val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader  = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual messages("error.summary.title")
        errorMessage must include(messages(errorMsg))
      }
    }

    "return 400 with error message " when {
      "aggregate data is there and invalid Tax Code is entered" when {
        " form submitted in english" in {
          return400("quick_calc.about_tax_code.wrong_tax_code", "110", cacheTaxCodeStatePension)
        }
        " form submitted in welsh" in {
          return400("quick_calc.about_tax_code.wrong_tax_code", "110", cacheTaxCodeStatePension, "cy")
        }
      }

      "No aggregate data and invalid Tax Code is entered" when {
        " form submitted in english" in {
          return400("quick_calc.about_tax_code.wrong_tax_code", "110")
        }
        " form submitted in welsh" in {
          return400("quick_calc.about_tax_code.wrong_tax_code", "110", None, "cy")
        }
      }

      "No aggregate data and invalid Tax Code Prefix is entered" when {
        " form submitted in english" in {
          return400("quick_calc.about_tax_code.wrong_tax_code_prefix", "X9999")
        }
        " form submitted in welsh" in {
          return400("quick_calc.about_tax_code.wrong_tax_code_prefix", "X9999", None, "cy")
        }
      }

      "No aggregate data and invalid Tax Code Suffix is entered" when {
        " form submitted in english" in {
          return400("quick_calc.about_tax_code.wrong_tax_code_suffix", "9999A")
        }
        " form submitted in welsh" in {
          return400("quick_calc.about_tax_code.wrong_tax_code_suffix", "9999A", None, "cy")
        }
      }

      "No aggregate data and Tax Code entered is 99999L is entered" when {
        " form submitted in english" in {
          return400("quick_calc.about_tax_code.wrong_tax_code", "99999L")
        }
        " form submitted in welsh" in {
          return400("quick_calc.about_tax_code.wrong_tax_code", "99999L", None, "cy")
        }
      }
    }

    "return 303" when {
      def test303(
        cacheFetchData: Option[QuickCalcAggregateInput] = None,
        formData:       Map[String, String]
      ) = {
        val mockCache = MockitoSugar.mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheFetchData)
        when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()
        implicit val messages: Messages = messagesThing(application)
        running(application) {
          val request = FakeRequest(POST, routes.TaxCodeController.submitTaxCodeForm.url)
            .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCSRFToken

          val result = route(application, request).get

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).get mustEqual routes.YouHaveToldUsNewController.summary.url
        }
      }

      "current aggregate data is present and redirect to the You Have Told Us page" in {
        test303(cacheTaxCodeStatePension, Map("hasTaxCode" -> "true", "taxCode" -> "K425"))
      }

      "with no aggregate data and redirect to you have told us page" in {
        test303(formData = Map("hasTaxCode" -> "true", "taxCode" -> "K425"))
      }

      "with current aggregate data and redirect to Summary Result Page" in {
        test303(cacheTaxCodeStatePensionSalary, Map("hasTaxCode" -> "true", "taxCode" -> "K425"))
      }
      "to check your answer page if No to tax code" in {
        test303(cacheTaxCodeStatePensionSalary.map(_.copy(savedTaxCode = None)), Map("hasTaxCode" -> "false"))
      }
    }

  }

}
