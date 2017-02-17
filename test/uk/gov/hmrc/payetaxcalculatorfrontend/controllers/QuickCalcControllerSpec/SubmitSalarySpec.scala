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
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.model.{Daily, Hourly, Salary, Yearly}
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._
import uk.gov.hmrc.play.http.SessionKeys

class SubmitSalarySpec extends AppUnitGenerator {

  "Submit Salary Form" should {
    "return 400, with current list of aggregate data and an error message for invalid Salary" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeAndIsOverStatePension)
      val formSalary = Salary.form
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))
      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size

      val expectedNumberOfRows = 1 + aggregateListOnlyTaxCodeAndStatePension.size //Including header

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe expectedNumberOfRows
      actualErrorMessage shouldBe expectedFieldErrorMessage
    }

    "return 400, with empty list of aggregate data and an error message for invalid Salary" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedFieldErrorMessage
    }

    "return 400 and error message when Salary submitted is \"9.999\" " in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Yearly(9.999))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedInvalidSalaryErrorMessage
    }

    "return 400 and error message when Salary submitted is \"-1\" " in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Yearly(-1))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedNegativeNumberErrorMessage
    }

    "return 400 and error message when Salary submitted is \"10,000,000.00\"" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Yearly(10000000.00))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedMaxGrossPayErrorMessage
    }

    "return 400 and error message when Hourly Salary submitted is -1" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Hourly(0,1))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedMinHourlyRateErrorMessage
    }

    "return 400 and error message when Daily Salary submitted is -1" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Daily(0,1))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedMinDailyRateErrorMessage
    }

    "return 400 and error message when Days in a Week is -1" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Daily(1,-1))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedMinDaysAWeekErrorMessage
    }

    "return 400 and error message when Hours in a Week is -1" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Hourly(1,-1))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedMinHoursAWeekErrorMessage
    }

    "return 400 and error message when Days in a Week is 8" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Daily(1,8))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedMaxDaysAWeekErrorMessage
    }

    "return 400 and error message when Hours in a Week is 169" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Hourly(1,169))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status
      val parseHtml = Jsoup.parse(contentAsString(postResult))

      val actualNumberOfRows = parseHtml.getElementsByTag("tr").size
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualNumberOfRows shouldBe 0
      actualErrorMessage shouldBe expectedMaxHoursAWeekErrorMessage
    }

    "return 303, with new Salary \"Yearly Salary £20000\" saved on the current list of aggregate data without State Pension answer and redirect to Summary Result Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
      val formSalary = Salary.form.fill(Yearly(20000))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/summary-result"
      val actualRedirect = redirectLocation(postResult).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with new Salary data e.g. \"Yearly Salary £20000\" saved on a new list of aggregate data and redirect to Summary Result Page" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Yearly(20000))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/summary-result"
      val actualRedirect = redirectLocation(postResult).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with new Salary data \"Yearly Salary £20000\" saved on the complete list of aggregate data and redirect to Summary Result Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
      val formSalary = Salary.form.fill(Yearly(20000))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))
      val postResult = postAction(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/summary-result"
      val actualRedirect = redirectLocation(postResult).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }
  }
}
