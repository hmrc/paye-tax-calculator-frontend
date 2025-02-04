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

import forms.PostGraduateLoanFormProvider
import models.{PostgraduateLoanContributions, QuickCalcAggregateInput}
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
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.PostGraduatePlanContributionView

import scala.concurrent.Future

class PostgraduateLoanContributionsControllerSpec
    extends BaseSpec
    with AnyWordSpecLike
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with CSRFTestHelper {

  val formProvider = new PostGraduateLoanFormProvider()
  val form: Form[PostgraduateLoanContributions] = formProvider()

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

  "Show Postgraduate Loans form" should {
    "return 303 with an empty list of aggregate data" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.PostgraduateController.showPostgraduateForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.SalaryController.showSalaryForm.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }

    }

    "return 200" when {

      def return200(
        fetchResponse: Option[QuickCalcAggregateInput],
        lang:          String = "en",
        isFormFilled:  Boolean = false
      ) = {
        val mockCache = MockitoSugar.mock[QuickCalcCache]

        when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        val formFilled =
          if (isFormFilled)
            form.fill(
              cacheTaxCodeStatePensionSalaryStudentLoanPostGrad.get.savedPostGraduateLoanContributions.get
            )
          else form

        implicit val messages: Messages = messagesThing(application, lang)

        running(application) {

          val request = FakeRequest(
            GET,
            routes.PostgraduateController.showPostgraduateForm.url
          ).withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", lang))
            .withCSRFToken

          val result = route(application, request).get

          val view = application.injector.instanceOf[PostGraduatePlanContributionView]

          val doc: Document = Jsoup.parse(contentAsString(result))
          val title         = doc.select(".govuk-heading-xl").text
          val subheading    = doc.select(".govuk-fieldset__heading").text
          val radiosChecked = doc.select(".govuk-radios__input[checked]")
          val radios        = doc.select(".govuk-radios__item")
          val button        = doc.select(".govuk-button").text
          val deskpro       = doc.select(".govuk-link")

          status(result) mustEqual OK
          title must include(messages("quick_calc.postgraduateLoan.header"))
          subheading mustEqual (messages("quick_calc.postgraduateLoan.subheading"))
          radios.get(0).text() mustEqual (messages("quick_calc.over_state_pension_age.yes"))
          radios.get(1).text() mustEqual (messages("quick_calc.over_state_pension_age.no"))
          button mustEqual (messages("continue"))
          if (lang == "cy")
            deskpro.text()    must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
          else deskpro.text() must include("Is this page not working properly? (opens in new tab)")
          if (isFormFilled) radiosChecked.attr("value") mustEqual ("true")

          removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
            view(
              formFilled
            )(request, messagesThing(application, lang)).toString
          )
          verify(mockCache, times(1)).fetchAndGetEntry()(any())

        }
      }
      "existing list of aggregate data containing Tax Code, pension and student loan contributions but no postgraduate " when {
        "form is in English" in {
          return200(
            cacheTaxCodeStatePensionSalaryStudentLoanPostGrad.map(_.copy(savedPostGraduateLoanContributions = None))
          )
        }
        "form is in Welsh" in {
          return200(
            cacheTaxCodeStatePensionSalaryStudentLoanPostGrad.map(_.copy(savedPostGraduateLoanContributions = None)),
            "cy"
          )
        }
      }

      "existing list of aggregate data containing Tax Code, pension and student loan contributions and postrgrad question answered " when {
        "form is in English" in {
          return200(cacheTaxCodeStatePensionSalaryStudentLoanPostGrad, isFormFilled = true)
        }
        "form is in Welsh" in {
          return200(
            cacheTaxCodeStatePensionSalaryStudentLoanPostGrad,
            "cy",
            true
          )
        }
      }
    }

  }

  "Submit Postgraduate Loans Form" should {
    "return 303, and redirect to the you have told us page if no option is picked" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePensionSalaryStudentLoanPostGrad
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("havePostGraduatePlan" -> "false")

        val request = FakeRequest(POST, routes.PostgraduateController.submitPostgradLoanForm.url)
          .withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }
    "return 303, with no aggregate data and redirect to You have told us page" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)
      when(mockCache.save(any())(any())) thenReturn Future.successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {
        val formData = Map("hasPostgraduatePlan" -> "false")

        val request = FakeRequest(POST, routes.PostgraduateController.submitPostgradLoanForm.url)
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
