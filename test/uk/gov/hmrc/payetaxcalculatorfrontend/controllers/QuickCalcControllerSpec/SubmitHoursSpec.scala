/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.{Hours, Salary}
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._
import uk.gov.hmrc.play.http.SessionKeys

class SubmitHoursSpec extends AppUnitGenerator {

  "Submit Hours Form" should {

    "return 400 and error message when empty Hours Form submission" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryInHoursForm
      val action = await(controller.submitHoursAWeek(1))

      val days = Map("amount" -> "1", "howManyAWeek" -> "")

      val result = action(request
        .withFormUrlEncodedBody(formSalary.bind(days).data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualHeaderMessage = parseHtml.getElementById("how-many-hours-a-week-error-link").text()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedEmptyHoursErrorMessage
      actualHeaderMessage shouldBe expectedEmptyHoursHeaderMessage
    }

    "return 400 and error message when Hours in a Week is 0" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryInHoursForm
      val action = await(controller.submitHoursAWeek(1))

      val days = Map("amount" -> "1", "howManyAWeek" -> "0")

      val result = action(request
        .withFormUrlEncodedBody(formSalary.bind(days).data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualHeaderMessage = parseHtml.getElementById("how-many-hours-a-week-error-link").text()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedMinHoursAWeekErrorMessage
      actualHeaderMessage shouldBe expectedInvalidPeriodAmountHeaderMessage
    }


    "return 400 and error message when Hours in a Week is 169" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryInHoursForm.fill(Hours(1,169))
      val action = await(controller.submitHoursAWeek(1))

      val result = action(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualHeaderMessage = parseHtml.getElementById("how-many-hours-a-week-error-link").text()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedMaxHoursAWeekErrorMessage
      actualHeaderMessage shouldBe expectedInvalidPeriodAmountHeaderMessage
    }

    "return 400 and error message when Hours in a Week is 1.5" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryInHoursForm
      val action = await(controller.submitHoursAWeek(1))

      val daily = Map("amount"->"1", "howManyAWeek" -> "1.5")

      val result = action(request
        .withFormUrlEncodedBody(formSalary.bind(daily).data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualHeaderMessage = parseHtml.getElementById("how-many-hours-a-week-error-link").text()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedWholeNumberHourlyErrorMessage
      actualHeaderMessage shouldBe expectedEmptyHoursHeaderMessage
    }

    "return 303, with new Hours worked, 40 and non-existent aggregate" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryBaseForm
      val action = await(controller.submitHoursAWeek(1))

      val daily = Map("amount"->"1", "howManyAWeek" -> "40")

      val result = action(request
        .withFormUrlEncodedBody(formSalary.bind(daily).data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val expectedRedirect = s"${baseURL}state-pension"
      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with new Hours worked, 5 and non-existent aggregate" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryBaseForm
      val action = await(controller.submitHoursAWeek(1))

      val daily = Map("amount"->"1", "howManyAWeek" -> "5")

      val result = action(request
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
