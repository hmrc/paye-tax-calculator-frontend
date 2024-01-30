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

import com.codahale.metrics.SharedMetricRegistries
import forms.forms.RemoveTaxCodeFormProvider
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
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
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.RemoveItemView

import scala.concurrent.Future

class RemoveTaxCodeControllerSpec
  extends BaseSpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with CSRFTestHelper {

  val taxCodeQueryParam = "taxcode"
  val formProvider = new RemoveTaxCodeFormProvider()
  val form: Form[Boolean] = formProvider(taxCodeQueryParam)

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "The remove item page with taxcode set as the query param" should {
    "return 200 - Ok with an empty form" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      val request = FakeRequest(
        GET,
        routes.RemoveItemController.showRemoveItemForm(taxCodeQueryParam).url
      ).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

      val result = route(application, request).get

      status(result) mustEqual OK

      }

    "return 303 See Other and redirect to the salary page with no aggregate data" in {
      SharedMetricRegistries.clear()
      val mockCache = MockitoSugar.mock[QuickCalcCache]

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
          routes.RemoveItemController.showRemoveItemForm(taxCodeQueryParam).url
        ).withHeaders(HeaderNames.xSessionId -> "test")

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.SalaryController
          .showSalaryForm
          .url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }

    }
    }

  "Submit Remove Tax Code Controller" should {

    "return 400 for invalid form answer and current list of aggregate data" in {
      SharedMetricRegistries.clear()
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        val request = FakeRequest(
          POST,
          routes.RemoveItemController.submitRemoveItemForm(taxCodeQueryParam).url
        ).withFormUrlEncodedBody(form.data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader =
          parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true
        errorMessage.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true

      }

    }

    "return 400 for invalid form answer and empty list of aggregate data" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

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
          routes.RemoveItemController.submitRemoveItemForm(taxCodeQueryParam).url
        ).withFormUrlEncodedBody(form.data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader =
          parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true
        errorMessage.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true
      }
    }

  }

}
