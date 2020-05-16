/*
 * Copyright 2020 HM Revenue & Customs
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

import config.AppConfig
import forms.Salary
import org.jsoup.Jsoup
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, redirectLocation, status, stubControllerComponents, _}
import services.{Navigator, QuickCalcCache, SalaryService}
import setup.QuickCalcCacheSetup.{baseURL, cacheEmpty, cacheReturnTaxCode, cacheReturnTaxCodeStatePension, cacheReturnTaxCodeStatePensionSalary}
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import views.html.pages.SalaryView
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito.{times, verify, when}

import scala.concurrent.Future

class SalaryControllerSpec extends PlaySpec with TryValues with ScalaFutures with IntegrationPatience with MockitoSugar {

//  "Submit Salary Form" should {
//
//    "return 400, with no aggregate data and empty Salary Form submission" in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//
//      val controller = new SalaryController(messagesApi, cacheEmpty, stubControllerComponents(), salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm
//      val action     = await(controller.submitSalaryAmount())
//
//      val formData = Map("value" -> "", "period" -> "")
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.bind(formData).data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderPeriodErrorMessage   = parseHtml.getElementById("salary-period-error-link").text()
//      val actualHeaderGrossPayErrorMessage = parseHtml.getElementById("salary-amount-error-link").text()
//      val actualGrossPayErrorMessage       = parseHtml.getElementById("pay-amount-inline-error").text()
//      val actualPeriodPayErrorMessage      = parseHtml.getElementById("period-inline-error").text()
//
//      status                           shouldBe 400
//      actualHeaderPeriodErrorMessage   shouldBe expectedNotChosenPeriodHeaderMesssage
//      actualHeaderGrossPayErrorMessage shouldBe expectedInvalidEmptyGrossPayHeaderMessage
//      actualGrossPayErrorMessage       shouldBe expectedEmptyGrossPayErrorMessage
//      actualPeriodPayErrorMessage      shouldBe expectedNotChosenPeriodErrorMessage
//    }
//
//    "return 400, with current list of aggregate data and an error message for invalid Salary" in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//
//      val controller = new SalaryController(messagesApi, cacheReturnTaxCodeStatePension, stubControllerComponents(),salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm
//      val action     = await(controller.submitSalaryAmount())
//
//      val formData = Map("value" -> "", "period" -> "yearly")
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.bind(formData).data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderGrossPayErrorMessage = parseHtml.getElementById("salary-amount-error-link").text()
//      val actualErrorMessage               = parseHtml.getElementsByClass("error-notification").text()
//
//      status                           shouldBe 400
//      actualHeaderGrossPayErrorMessage shouldBe expectedInvalidEmptyGrossPayHeaderMessage
//      actualErrorMessage               shouldBe expectedEmptyGrossPayErrorMessage
//    }
//
//    "return 400, with empty list of aggregate data and an error message for invalid Salary" in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//
//      val controller = new SalaryController(messagesApi, cacheEmpty, stubControllerComponents(), salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm
//      val action     = await(controller.submitSalaryAmount())
//
//      val formData = Map("value" -> "", "period" -> "yearly")
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.bind(formData).data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderGrossPayErrorMessage = parseHtml.getElementById("salary-amount-error-link").text()
//      val actualErrorMessage               = parseHtml.getElementsByClass("error-notification").text()
//
//      status                           shouldBe 400
//      actualHeaderGrossPayErrorMessage shouldBe expectedInvalidEmptyGrossPayHeaderMessage
//      actualErrorMessage               shouldBe expectedEmptyGrossPayErrorMessage
//    }
//
//    "return 400 and error message when Salary submitted is \"9.999\" " in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//
//      val controller = new SalaryController(messagesApi, cacheEmpty, stubControllerComponents(), salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm.fill(Salary(9.999, "yearly", None))
//      val action     = await(controller.submitSalaryAmount())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderGrossPayErrorMessage = parseHtml.getElementById("salary-amount-error-link").text()
//      val actualErrorMessage               = parseHtml.getElementsByClass("error-notification").text()
//
//      status                           shouldBe 400
//      actualErrorMessage               shouldBe expectedMaxGrossPayErrorMessage
//      actualHeaderGrossPayErrorMessage shouldBe expectedInvalidGrossPayHeaderMessage
//    }
//
//    "return 400 and error message when Salary submitted is \"-1\" " in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//
//      val controller = new SalaryController(messagesApi, cacheEmpty, stubControllerComponents(), salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm.fill(Salary(-1, "yearly", None))
//      val action     = await(controller.submitSalaryAmount())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderGrossPayErrorMessage = parseHtml.getElementById("salary-amount-error-link").text()
//      val actualErrorMessage               = parseHtml.getElementsByClass("error-notification").text()
//
//      status                           shouldBe 400
//      actualErrorMessage               shouldBe expectedNegativeNumberErrorMessage
//      actualHeaderGrossPayErrorMessage shouldBe expectedInvalidGrossPayHeaderMessage
//    }
//
//    "return 400 and error message when Salary submitted is \"10,000,000.00\"" in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//      val controller = new SalaryController(messagesApi, cacheEmpty, stubControllerComponents(), salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm.fill(Salary(10000000.00, "yearly", None))
//      val action     = await(controller.submitSalaryAmount())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderGrossPayErrorMessage = parseHtml.getElementById("salary-amount-error-link").text()
//      val actualErrorMessage               = parseHtml.getElementsByClass("error-notification").text()
//
//      status                           shouldBe 400
//      actualErrorMessage               shouldBe expectedMaxGrossPayErrorMessage
//      actualHeaderGrossPayErrorMessage shouldBe expectedInvalidGrossPayHeaderMessage
//    }
//
//    """return 303, with new Yearly Salary "£20000", current list of aggregate data without State Pension Answer and redirect to State Pension Page""" in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//      val controller = new SalaryController(messagesApi, cacheReturnTaxCode, stubControllerComponents(), salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm.fill(Salary(20000, "yearly", None))
//      val action     = await(controller.submitSalaryAmount())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status = result.header.status
//
//      val expectedRedirect = s"${baseURL}state-pension"
//      val actualRedirect   = redirectLocation(result).get
//
//      status         shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//
//    """return 303, with new Yearly Salary data "£20000" saved on a new list of aggregate data and redirect to State Pension Page""" in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//
//      val controller = new SalaryController(messagesApi, cacheEmpty, stubControllerComponents(),salaryService ,navigator)
//      val formSalary = Salary.salaryBaseForm.fill(Salary(20000, "yearly", None))
//      val action     = await(controller.submitSalaryAmount())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status = result.header.status
//
//      val expectedRedirect = s"${baseURL}state-pension"
//      val actualRedirect   = redirectLocation(result).get
//
//      status         shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//
//    """return 303, with new Yearly Salary data "£20000" saved on the complete list of aggregate data and redirect to State Pension Page""" in {
//      val salaryService: SalaryService   = appInjector.instanceOf[SalaryService]
//
//      val controller =
//        new SalaryController(messagesApi, cacheReturnTaxCodeStatePensionSalary, stubControllerComponents(), salaryService, navigator)
//      val formSalary = Salary.salaryBaseForm.fill(Salary(20000, "yearly", None))
//      val action     = await(controller.submitSalaryAmount())
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status = result.header.status
//
//      val expectedRedirect = s"${baseURL}your-answers"
//      val actualRedirect   = redirectLocation(result).get
//
//      status         shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//  }
lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(app:Application): Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)



  "Show Salary Form" should {
    "return 200, with current list of aggregate data containing Tax Code: 1150L, \"YES\" for is not Over65, 20000 a Year for Salary" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePensionSalary)

      val application  = new GuiceApplicationBuilder().overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      val form = Salary.salaryBaseForm.fill(cacheTaxCodeStatePensionSalary.value.savedSalary.get)
      running(application) {

        val request = FakeRequest(GET, routes.SalaryController.showSalaryForm().url).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

        val result = route(application, request).value

        val view = application.injector.instanceOf[SalaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form)(request, messagesThing(application)).toString
      }


    }

    "return 200, with empty list of aggregate data" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(None)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      val form = Salary.salaryBaseForm
      running(application) {

        val request = FakeRequest(GET, routes.SalaryController.showSalaryForm().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        val view = application.injector.instanceOf[SalaryView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
        view(form)(request, messagesThing(application)).toString
      }
    }
  }
}
