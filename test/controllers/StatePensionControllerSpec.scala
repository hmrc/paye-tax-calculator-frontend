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

  def messagesThing(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show State Pension Form" should {
    "return 200, with existing list of aggregate data" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePensionSalary
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      val formFilled = form.fill(
        cacheTaxCodeStatePensionSalary.value.savedIsOverStatePensionAge.get
      )
      running(application) {

        val request = FakeRequest(
          GET,
          routes.StatePensionController.showStatePensionForm.url
        ).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

        val result = route(application, request).value

        val view = application.injector.instanceOf[StatePensionView]

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(
            formFilled,
            aggregateCompleteListYearly
          )(request, messagesThing(application)).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

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

    "Submit State Pension Form" should {

      "return 400 for invalid form answer and current list of aggregate data" in {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
          cacheTaxCodeStatePension
        )

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val request = FakeRequest(
            POST,
            routes.StatePensionController.submitStatePensionForm.url
          ).withFormUrlEncodedBody(form.data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCSRFToken

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val parseHtml = Jsoup.parse(contentAsString(result))

          val errorHeader =
            parseHtml.getElementsByClass("govuk-error-summary__title").text()
          val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
          val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()
          errorHeader mustEqual "There is a problem"
          errorMessageLink.contains(expectedInvalidStatePensionAnswer) mustEqual true
          errorMessage.contains(expectedInvalidStatePensionAnswer) mustEqual true
        }
      }
      "return 400 for invalid form answer and current list of aggregate data in welsh" in {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
          cacheTaxCodeStatePension
        )

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val request = FakeRequest(
            POST,
            routes.StatePensionController.submitStatePensionForm.url
          ).withFormUrlEncodedBody(form.data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", "cy"))
            .withCSRFToken

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val parseHtml = Jsoup.parse(contentAsString(result))

          val errorHeader =
            parseHtml.getElementsByClass("govuk-error-summary__title").text()
          val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
          val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()
          errorHeader mustEqual "Mae problem wedi codi"
          errorMessageLink.contains(expectedInvalidStatePensionAnswercy) mustEqual true
          errorMessage.contains(expectedInvalidStatePensionAnswercy) mustEqual true
        }
      }

      "return 400 for invalid form answer and empty list of aggregate data" in {
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
            POST,
            routes.StatePensionController.submitStatePensionForm.url
          ).withFormUrlEncodedBody(form.data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCSRFToken

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val parseHtml = Jsoup.parse(contentAsString(result))

          val errorHeader =
            parseHtml.getElementsByClass("govuk-error-summary__title").text()
          val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
          val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

          errorHeader mustEqual "There is a problem"
          errorMessageLink.contains(expectedInvalidStatePensionAnswer) mustEqual true
          errorMessage.contains(expectedInvalidStatePensionAnswer) mustEqual true
        }
      }
      "return 400 for invalid form answer and empty list of aggregate data in welsh" in {
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
            POST,
            routes.StatePensionController.submitStatePensionForm.url
          ).withFormUrlEncodedBody(form.data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", "cy"))
            .withCSRFToken

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val parseHtml = Jsoup.parse(contentAsString(result))

          val errorHeader =
            parseHtml.getElementsByClass("govuk-error-summary__title").text()
          val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
          val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

          errorHeader mustEqual "Mae problem wedi codi"
          errorMessageLink.contains(expectedInvalidStatePensionAnswercy) mustEqual true
          errorMessage.contains(expectedInvalidStatePensionAnswercy) mustEqual true
        }
      }


      "return 303, with an answer \"No\" saved on existing list of aggregate data without Salary and redirect to Salary Page" in {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
          cacheTaxCode
        )
        when(mockCache.save(any())(any())) thenReturn Future.successful(
          Done
        )

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val formData = Map("overStatePensionAge" -> "false")

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

      "return 303, with an answer \"No\" saved on existing list of aggregate data without Salary and redirect to Salary Page in Welsh" in {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
          cacheTaxCode
        )
        when(mockCache.save(any())(any())) thenReturn Future.successful(
          Done
        )

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val formData = Map("overStatePensionAge" -> "false")

          val request = FakeRequest(
            GET,
            routes.StatePensionController.showStatePensionForm.url
          ).withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", "cy"))
            .withCSRFToken

          val result = route(application, request).value

          val view = application.injector.instanceOf[StatePensionView]

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
          verify(mockCache, times(1)).fetchAndGetEntry()(any())
        }
      }

      "return 303, with an answer \"Yes\" for being Over 65 saved on a new list of aggregate data and redirect Salary Page" in {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
          cacheTaxCode
        )
        when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val formData = Map("overStatePensionAge" -> "true")

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
      "return 303, with an answer \"Yes\" for being Over 65 saved on a new list of aggregate data and redirect Salary Page in welsh" in {
        val mockCache = mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
          cacheTaxCode
        )
        when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)

        running(application) {

          val formData = Map("overStatePensionAge" -> "true")

          val request = FakeRequest(
            GET,
            routes.StatePensionController.showStatePensionForm.url
          ).withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", "cy"))
            .withCSRFToken

          val result = route(application, request).value

          val view = application.injector.instanceOf[StatePensionView]

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SalaryController.showSalaryForm.url
          verify(mockCache, times(1)).fetchAndGetEntry()(any())
        }
      }
      "return 200, with empty list of aggregate data in Welsh" in {
        val mockCache = mock[QuickCalcCache]
        println("print cache=" + mockCache)

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheSalaryStatePensionTaxCode)

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        running(application) {

          val request = FakeRequest(GET, routes.StatePensionController.showStatePensionForm.url)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", "cy"))
            .withCSRFToken

          println("print request=" + request)
          val result = route(application, request).value
          println("print result=" + result)
          val doc: Document = Jsoup.parse(contentAsString(result))
          println("print document = "+ doc)
          val header = doc.select(".govuk-header").text
          val betaBanner = doc.select(".govuk-phase-banner").text
          val heading = doc.select(".govuk-fieldset__heading").text
          val radios  = doc.select(".govuk-radios__item")
          val details = doc.select(".govuk-details__summary-text").text()
          val button  = doc.select(".govuk-button").text
          val deskpro = doc.select(".govuk-link")
          println("print header=" + header)
          header must include("Cyfrifiannell Treth TWE")
          betaBanner must include("Gwasanaeth newydd yw hwn – bydd eich adborth yn ein helpu i’w wella.")
          heading mustEqual("A ydych dros oedran Pensiwn y Wladwriaeth?")
          button mustEqual ("Yn eich blaen")
          radios.get(0).text mustEqual ("Iawn")
          radios.get(1).text mustEqual ("Na")
          println("summary test="+details)
          details must include("Pam rydym yn gofyn hyn")
          deskpro.text() must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")

          status(result) mustEqual (OK)

        }

      }
    }
  }
}
