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
import models.QuickCalcAggregateInput
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.PensionContributionsPercentageView

import scala.concurrent.Future

class PensionContributionsPercentageControllerSpec
    extends BaseSpec
    with AnyWordSpecLike
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with CSRFTestHelper {

  val formProvider = new PensionContributionFormProvider()
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

  "Show Pension Contributions Form" should {
    "return 300 and an empty list of aggregate data" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.PensionContributionsPercentageController.showPensionContributionForm.url)
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

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheSalaryTaxCodeSavedPensionContributionsPercentage
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.PensionContributionsPercentageController.showPensionContributionForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[PensionContributionsPercentageView]

        val formFilled =
          form.fill(cacheSalaryTaxCodeSavedPensionContributionsPercentage.get.savedPensionContributions.get)

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(formFilled,
               cacheSalaryTaxCodeSavedPensionContributionsPercentage.get.additionalQuestionItems()(messages,
                                                                                                   mockAppConfig))(
            request,
            messagesThing(application)
          ).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }
    "return 200 and a list of current aggregate data containing Tax Code and pension and tax code answered in Welsh" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheSalaryTaxCodeSavedPensionContributionsPercentage
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, "cy")

      running(application) {

        val request = FakeRequest(GET, routes.PensionContributionsPercentageController.showPensionContributionForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", "cy"))
          .withCSRFToken

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title   = doc.select(".govuk-heading-xl").text
        val para    = doc.select(".govuk-body").text()
        val label   = doc.select(".govuk-label").text()
        val hint    = doc.select(".govuk-hint").text()
        val button  = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        val view = application.injector.instanceOf[PensionContributionsPercentageView]

        val formFilled =
          form.fill(cacheSalaryTaxCodeSavedPensionContributionsPercentage.get.savedPensionContributions.get)

        status(result) mustEqual OK
        title must include(messages("quick_calc.you_have_told_us.about_pension_contributions.label"))
        para  must include(messages("quick_calc.pensionContributionsPercentage.subheading"))
        label mustEqual (messages("quick_calc.pensionContributionsPercentage.input.heading"))
        hint mustEqual (messages("quick_calc.pensionContributionsPercentage.hint"))
        para must include(messages("quick_calc.pensionContributionsPercentage.link"))
        button mustEqual (messages("continue"))
        deskpro.text() must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(formFilled,
               cacheSalaryTaxCodeSavedPensionContributionsPercentage.get.additionalQuestionItems()(messages,
                                                                                                   mockAppConfig))(
            request,
            messagesThing(application, "cy")
          ).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }
  }

  "Submit Pension Contributions Form" should {

    "return 400" when {
      def test400(
        lang:          String = "en",
        fetchResponse: Option[QuickCalcAggregateInput],
        percentage:    String
      ) = {
        val mockCache = MockitoSugar.mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()
        implicit val messages: Messages = messagesThing(application, lang)
        running(application) {

          val formData = Map("gaveUsPensionPercentage" -> "true", "monthlyPensionContributions" -> percentage)

          val request = FakeRequest(POST, routes.PensionContributionsPercentageController.submitPensionContribution.url)
            .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", lang))
            .withCSRFToken

          val result = route(application, request).get

          status(result) mustEqual BAD_REQUEST

          val parseHtml = Jsoup.parse(contentAsString(result))

          val errorHeader      = parseHtml.getElementsByClass("govuk-error-summary__title").text()
          val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
          val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

          errorHeader mustEqual messages("error.summary.title")
          errorMessageLink.contains(messages("quick_calc.pensionContributionError.invalidFormat")) mustEqual true
          errorMessage.contains(messages("quick_calc.pensionContributionError.invalidFormat")) mustEqual true
        }
      }
      "there is aggregate data and an error message for invalid pension amount" in {
        test400(fetchResponse = cacheSalaryTaxCodeSavedPensionContributionsPercentage, percentage = "40;")
      }
      "there is no aggregate data and an error message for invalid pension contributions" in {
        test400(fetchResponse = None, percentage = "40;")
      }
      "there is no aggregate data and an error message for pension contributions when value is more than 2dp" in {
        test400(fetchResponse = None, percentage = "40.023")
      }
      "there is aggregate data and an error message for invalid pension amount in welsh" in {
        test400(lang = "cy", fetchResponse = cacheSalaryTaxCodeSavedPensionContributionsPercentage, percentage = "40;")
      }
      "there is no aggregate data and an error message for invalid pension contributions in welsh" in {
        test400(lang = "cy", fetchResponse = None, percentage = "40;")
      }
      "there is no aggregate data and an error message for pension contributions when value is more than 2dp in welsh" in {
        test400(lang = "cy", fetchResponse = None, percentage = "40.023")
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
        val formData = Map("gaveUsPensionPercentage" -> "true")

        val request = FakeRequest(POST, routes.PensionContributionsPercentageController.submitPensionContribution.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.YouHaveToldUsNewController.summary.url
      }
    }
  }
}
