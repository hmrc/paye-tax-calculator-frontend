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
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.Salary
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._
import uk.gov.hmrc.play.http.SessionKeys

class SubmitSalarySpec extends AppUnitGenerator {

  "Submit Salary Form" should {
    "return 400, with current list of aggregate data and an error message for invalid Salary" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeStatePension)
      val formSalary = Salary.salaryBaseForm
      val action = await(csrfAddToken(controller.submitSalaryAmount()))

      val formData = Map("value" -> "", "period" -> "yearly")

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.bind(formData).data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedGrossPayErrorMessage
    }

    "return 400, with empty list of aggregate data and an error message for invalid Salary" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryBaseForm
      val action = await(csrfAddToken(controller.submitSalaryAmount()))

      val formData = Map("value" -> "", "period" -> "yearly")

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.bind(formData).data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedGrossPayErrorMessage
    }

    "return 400 and error message when Salary submitted is \"9.999\" " in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryBaseForm.fill(Salary(9.999,"yearly", None))
      val action = await(csrfAddToken(controller.submitSalaryAmount()))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedMaxGrossPayErrorMessage
    }

    "return 400 and error message when Salary submitted is \"-1\" " in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryBaseForm.fill(Salary(-1,"yearly", None))
      val action = await(csrfAddToken(controller.submitSalaryAmount()))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedNegativeNumberErrorMessage
    }

    "return 400 and error message when Salary submitted is \"10,000,000.00\"" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryBaseForm.fill(Salary(10000000.00,"yearly", None))
      val action = await(csrfAddToken(controller.submitSalaryAmount()))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formSalary.data.toSeq: _*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status
      val parseHtml = Jsoup.parse(contentAsString(result))

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedMaxGrossPayErrorMessage
    }

    """return 303, with new Yearly Salary "£20000", current list of aggregate data without State Pension Answer and redirect to State Pension Page""" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
      val formSalary = Salary.salaryBaseForm.fill(Salary(20000,"yearly", None))
      val action = await(csrfAddToken(controller.submitSalaryAmount()))

      val result = action(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary").withSession("csrfToken" -> "someToken"))

      val status = result.header.status

      val expectedRedirect = s"${baseURL}state-pension"
      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    """return 303, with new Yearly Salary data "£20000" saved on a new list of aggregate data and redirect to State Pension Page""" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.salaryBaseForm.fill(Salary(20000,"yearly", None))
      val action = await(csrfAddToken(controller.submitSalaryAmount()))

      val result = action(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary").withSession("csrfToken" -> "someToken"))

      val status = result.header.status

      val expectedRedirect = s"${baseURL}state-pension"
      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    """return 303, with new Yearly Salary data "£20000" saved on the complete list of aggregate data and redirect to State Pension Page""" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeStatePensionSalary)
      val formSalary = Salary.salaryBaseForm.fill(Salary(20000,"yearly", None))
      val action = await(csrfAddToken(controller.submitSalaryAmount()))
      val result = action(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*).withSession("csrfToken" -> "someToken")
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = result.header.status

      val expectedRedirect = s"${baseURL}your-answers"
      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }
  }
}
