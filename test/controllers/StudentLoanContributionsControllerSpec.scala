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

import forms.StudentLoansFormProvider
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
import views.html.pages.StudentLoansContributionView

import scala.concurrent.Future


class StudentLoanContributionsControllerSpec extends BaseSpec
  with AnyWordSpecLike
  with TryValues
  with ScalaFutures
  with IntegrationPatience
  with MockitoSugar
  with CSRFTestHelper {

  val formProvider = new StudentLoansFormProvider()
  val form         = formProvider()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(app: Application, lang: String = "en"): Messages =
    if(lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
  else
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Student Loans form" should {
    "return 200 with an empty list of aggregate data" in {

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(GET, routes.StudentLoanContributionsController.showStudentLoansForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.SalaryController.showSalaryForm.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }

    }
    "return 200, with existing list of aggregate data" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheTaxCodeStatePensionSalaryStudentLoan
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      val formFilled = form.fill(
        cacheTaxCodeStatePensionSalaryStudentLoan.get.savedStudentLoanContributions.get
      )

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(
          GET,
          routes.StudentLoanContributionsController.showStudentLoansForm.url
        ).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[StudentLoansContributionView]

        status(result) mustEqual OK

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(
            formFilled
          )(request, messagesThing(application)).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }
    }
    "return 200 with correct welsh translation" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePensionSalaryStudentLoan)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, "cy")

      running(application) {

        val request = FakeRequest(GET, routes.StudentLoanContributionsController.showStudentLoansForm.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", "cy"))
          .withCSRFToken

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val header = doc.select(".govuk-header").text
        val betaBanner = doc.select(".govuk-phase-banner").text
        val heading = doc.select(".govuk-heading-xl").text
        val subHeading = doc.select(".govuk-fieldset__legend").text
        val radios = doc.select(".govuk-radios__item")
        val button  = doc.select(".govuk-button").text
        val deskproLink = doc.select(".govuk-link")
        val checkedRadios = doc.select(".govuk-radios__input[checked]")
        checkedRadios.attr("value") mustEqual ("plan one")

        header must include(messages("quick_calc.header.title"))
        betaBanner must include(messages("feedback.before"))
        betaBanner must include(messages("feedback.link"))
        betaBanner must include(messages("feedback.after"))
        heading mustEqual (messages("quick_calc.salary.studentLoan.header"))
        subHeading mustEqual (messages("quick_calc.salary.studentLoan.subheading"))
        radios.get(0).text mustEqual (messages("quick_calc.salary.studentLoan.plan1.text"))
        radios.get(1).text mustEqual (messages("quick_calc.salary.studentLoan.plan2.text"))
        radios.get(2).text mustEqual (messages("quick_calc.salary.studentLoan.plan4.text"))
        radios.get(3).text mustEqual (messages("quick_calc.salary.studentLoan.noneOfThese.text"))
        button mustEqual (messages("continue"))
        deskproLink.text must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        status(result) mustEqual (OK)
      }
    }
  }
}
