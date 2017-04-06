/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcControllerSpec

import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.filters.csrf.CSRFFilter
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.{Days, Salary}
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._
import uk.gov.hmrc.play.http.SessionKeys

class SubmitDaysSpec extends AppUnitGenerator {

  "Submit Days Form" should {
    "return 400 and error message when Days in a Week is 0" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)(new CSRFFilter)
      val formSalary = Salary.salaryInDaysForm
      val action = await(csrfAddToken(controller.submitDaysAWeek(1)))

      val days = Map("amount" -> "1", "howManyAWeek" -> "0")

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.bind(days).data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedMinDaysAWeekErrorMessage
    }


    "return 400 and error message when Days in a Week is 8" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)(new CSRFFilter)
      val formSalary = Salary.salaryInDaysForm.fill(Days(1,8))
      val action = await(csrfAddToken(controller.submitDaysAWeek(1)))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedMaxDaysAWeekErrorMessage
    }

    "return 400 and error message when Days in a Week is 1.5" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)(new CSRFFilter)
      val formSalary = Salary.salaryInDaysForm
      val action = await(csrfAddToken(controller.submitDaysAWeek(1)))

      val daily = Map("amount"->"1", "howManyAWeek" -> "1.5")

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.bind(daily).data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedWholeNumberDailyErrorMessage
    }

    "return 303, with new Days worked, 1 and non-existent aggregate" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)(new CSRFFilter)
      val formSalary = Salary.salaryInDaysForm
      val action = await(csrfAddToken(controller.submitDaysAWeek(1)))

      val daily = Map("amount"->"1", "howManyAWeek" -> "1")

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.bind(daily).data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val expectedRedirect = s"${baseURL}state-pension"
      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with new Days worked, 5 and non-existent aggregate" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)(new CSRFFilter)
      val formSalary = Salary.salaryInDaysForm
      val action = await(csrfAddToken(controller.submitDaysAWeek(1)))

      val daily = Map("amount"->"1", "howManyAWeek" -> "5")

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.bind(daily).data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val expectedRedirect = s"${baseURL}state-pension"
      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }
  }

}
