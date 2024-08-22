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

import forms.PensionContributionFormProvider
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.test.Helpers.{status, _}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.PensionContributionsFixedView

import scala.concurrent.Future

class PensionsContributionFixedControllerSpec
  extends BaseSpec
    with AnyWordSpecLike
  with TryValues
  with ScalaFutures
  with IntegrationPatience
    with MockitoSugar
  with CSRFTestHelper {

  val formProvider = new PensionContributionFormProvider()
  val form = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Pension Contributions Form" should {
    "return 200 and an empty list of aggregate data" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.PensionContributionsFixedController.showPensionContributionForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.SalaryController.showSalaryForm.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }
    "return 200 and a list of current aggregate data containing Tax Code and pension and tax code answered" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheSalaryTaxCodeSavedPensionContributionsFixed)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.PensionContributionsFixedController.showPensionContributionForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[PensionContributionsFixedView]

        val formFilled = form.fill(cacheSalaryTaxCodeSavedPensionContributionsFixed.get.savedPensionContributions.get)

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(formFilled,
            cacheSalaryStatePensionTaxCode.get.additionalQuestionItems()(messages, mockAppConfig))(
            request,
            messagesThing(application)
          ).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }
  }

  "Submit Pension Contributions Form" should {
    "return 400, with aggregate data and an error message for invalid pension amount" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheSalaryTaxCodeSavedPensionContributionsFixed)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("gaveUsPensionPercentage" -> "false", "monthlyPensionContributions" -> "40;", "yearlyContributionAmount" -> "480")

        val request = FakeRequest(POST, routes.PensionContributionsFixedController.submitPensionContribution.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidPensionsErrorMessage) mustEqual true
        errorMessage.contains(expectedInvalidPensionsErrorMessage) mustEqual true

      }
    }

    "return 400, with aggregate data and an error message for invalid pension contributiom when amount is over ten million" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheSalaryTaxCodeSavedPensionContributionsFixed)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("gaveUsPensionPercentage" -> "false", "monthlyPensionContributions" -> "1099999999", "yearlyContributionAmount" -> "480")

        val request = FakeRequest(POST, routes.PensionContributionsFixedController.submitPensionContribution.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidPensionMessageOverTenMill) mustEqual true
        errorMessage.contains(expectedInvalidPensionMessageOverTenMill) mustEqual true

      }
    }

    "return 400, with no aggregate data and an error message for invalid pension contributions" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("gaveUsPensionPercentage" -> "false", "monthlyPensionContributions" -> "40;", "yearlyContributionAmount" -> "480")

        val request = FakeRequest(POST, routes.PensionContributionsFixedController.submitPensionContribution.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidPensionsErrorMessage) mustEqual true
        errorMessage.contains(expectedInvalidPensionsErrorMessage) mustEqual true
      }
    }

    "return 400, with no aggregate data and an error message for pension contributions when value is more than 2dp" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val formData = Map("gaveUsPensionPercentage" -> "false", "monthlyPensionContributions" -> "40.023", "yearlyContributionAmount" -> "480")

        val request = FakeRequest(POST, routes.PensionContributionsFixedController.submitPensionContribution.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidPensionTwoDecimalPlaces) mustEqual true
        errorMessage.contains(expectedInvalidPensionTwoDecimalPlaces) mustEqual true
      }
    }
    "return 303, with to your answers page if No pension contributions entered" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheSalaryTaxCodeSavedPensionContributionsFixed.map(_.copy(savedPensionContributions = None))
      )
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {
        val formData = Map("gaveUsPensionPercentage" -> "false")

        val request = FakeRequest(POST, routes.PensionContributionsFixedController.submitPensionContribution.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.YouHaveToldUsController.summary.url
      }
    }
  }
}
