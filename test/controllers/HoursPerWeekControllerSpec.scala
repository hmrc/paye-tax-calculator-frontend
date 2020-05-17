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

import forms.{SalaryFormProvider, SalaryInHoursFormProvider}
import models.{Hours, QuickCalcAggregateInput}
import org.jsoup.Jsoup
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.QuickCalcCacheSetup.{baseURL, cacheEmpty, cacheTaxCodeStatePensionSalary}
import setup.{BaseSpec, QuickCalcCacheSetup}
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}
import views.html.pages.{HoursAWeekView, SalaryView}
import play.api.test.CSRFTokenHelper._

import scala.concurrent.Future

class HoursPerWeekControllerSpec extends PlaySpec
  with TryValues
  with ScalaFutures
  with IntegrationPatience
  with MockitoSugar {
  val formProvider = new SalaryInHoursFormProvider()
  val form: Form[Hours] = formProvider()
  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  def messagesForApp(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Hours Form" should {

    "return 200, with existing list of aggregate" in {
//      val agg    = QuickCalcCacheSetup.cacheTaxCodeStatePensionSalary.get
//      val result = controller.showHoursAWeekTestable(0, "")(request)(agg)
//      val status = result.header.status
//
//      status shouldBe 200

      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(cacheTaxCodeStatePensionSalary)

      val application: Application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesForApp(application)

      running(application) {

        val request: RequestHeader = FakeRequest(GET, routes.HoursPerWeekController.showHoursAWeek(0, "").url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val view = application.injector.instanceOf[HoursAWeekView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(0,form, "")(request, messagesForApp(application)).toString

        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }

    "return 200, with non-existing list of aggregate" in {
//      val agg = QuickCalcAggregateInput.newInstance
//
//      val result = controller.showHoursAWeekTestable(0, "")(request)(agg)
//      val status = result.header.status
//
//      status shouldBe 200
    }
  }


//  "Submit Hours Form" should {
//
//    "return 400 and error message when empty Hours Form submission" in {
//      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
//      val formSalary = Salary.salaryInHoursForm
//      val action     = await(controller.submitHoursAWeek(1))
//
//      val days = Map("amount" -> "1", "howManyAWeek" -> "")
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.bind(days).data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderMessage = parseHtml.getElementById("how-many-hours-a-week-error-link").text()
//      val actualErrorMessage  = parseHtml.getElementsByClass("error-notification").text()
//
//      status              shouldBe 400
//      actualErrorMessage  shouldBe expectedEmptyHoursErrorMessage
//      actualHeaderMessage shouldBe expectedEmptyHoursHeaderMessage
//    }
//
//    "return 400 and error message when Hours in a Week is 0" in {
//      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
//      val formSalary = Salary.salaryInHoursForm
//      val action     = await(controller.submitHoursAWeek(1))
//
//      val days = Map("amount" -> "1", "howManyAWeek" -> "0")
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.bind(days).data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val actualHeaderMessage = parseHtml.getElementById("how-many-hours-a-week-error-link").text()
//      val actualErrorMessage  = parseHtml.getElementsByClass("error-notification").text()
//
//      status              shouldBe 400
//      actualErrorMessage  shouldBe expectedMinHoursAWeekErrorMessage
//      actualHeaderMessage shouldBe expectedInvalidPeriodAmountHeaderMessage
//    }
//
//    "return 400 and error message when Hours in a Week is 169" in {
//      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
//      val formSalary = Salary.salaryInHoursForm.fill(Hours(1, 169))
//      val action     = await(controller.submitHoursAWeek(1))
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
//      val actualHeaderMessage = parseHtml.getElementById("how-many-hours-a-week-error-link").text()
//      val actualErrorMessage  = parseHtml.getElementsByClass("error-notification").text()
//
//      status              shouldBe 400
//      actualErrorMessage  shouldBe expectedMaxHoursAWeekErrorMessage
//      actualHeaderMessage shouldBe expectedInvalidPeriodAmountHeaderMessage
//    }
//
//    "return 303, with new Hours worked, 40.5 and non-existent aggregate" in {
//      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
//      val formSalary = Salary.salaryBaseForm
//      val action     = await(controller.submitHoursAWeek(1))
//
//      val daily = Map("amount" -> "1", "howManyAWeek" -> "40.5")
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.bind(daily).data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val expectedRedirect = s"${baseURL}state-pension"
//      val actualRedirect   = redirectLocation(result).get
//
//      status         shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//
//    "return 303, with new Hours worked, 5 and non-existent aggregate" in {
//      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
//      val formSalary = Salary.salaryBaseForm
//      val action     = await(controller.submitHoursAWeek(1))
//
//      val daily = Map("amount" -> "1", "howManyAWeek" -> "5")
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formSalary.bind(daily).data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-salary")
//      )
//
//      val status    = result.header.status
//      val parseHtml = Jsoup.parse(contentAsString(result))
//
//      val expectedRedirect = s"${baseURL}state-pension"
//      val actualRedirect   = redirectLocation(result).get
//
//      status         shouldBe 303
//      actualRedirect shouldBe expectedRedirect
//    }
//
//  }


}
