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
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.UserTaxCode
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.AppUnitGenerator
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._
import uk.gov.hmrc.play.http.SessionKeys

class SubmitTaxCodeSpec extends AppUnitGenerator {

  "Submit Tax Code Form" should {
    "return 400, current list of aggregate data and an error message for invalid Tax Code" in {
      val controller = new QuickCalcController(
        messages.messages, cacheReturnTaxCode)
      val formTax = UserTaxCode.form.fill(
        UserTaxCode(gaveUsTaxCode = true, Some("110")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withSession("csrfToken" -> "someToken").withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedSuffixTaxCodeErrorMessage
    }

    "return 400, empty list of aggregate data and an error message for invalid Tax Code" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("110")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedErrorMessage = "The tax code you have entered is not valid - it must end with the letter L, ‘M, ‘N, or T"

      val actualTableSize = parseHtml.getElementsByTag("tr").size()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedSuffixTaxCodeErrorMessage
      actualTableSize shouldBe 0
    }

    "return 400, empty list of aggregate data and an error message for invalid Tax Code Prefix" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true, Some("OO9999")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedErrorMessage = "There`s a problem with the tax code you`ve entered"

      val actualTableSize = parseHtml.getElementsByTag("tr").size()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedPrefixTaxCodeErrorMessage
      actualTableSize shouldBe 0
    }

    "return 400, empty list of aggregate data and an error message when user selects \"Yes\" but did not enter Tax code" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, None))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val actualTableSize = parseHtml.getElementsByTag("tr").size()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedInvalidTaxCodeErrorMessage
      actualTableSize shouldBe 0
    }

    "return 400, empty list of aggregate data and an error message when user selects \"Yes\" but Tax Code entered is 99999L" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("99999L")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val actualTableSize = parseHtml.getElementsByTag("tr").size()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedWrongNumberTaxCodeErrorMessage
      actualTableSize shouldBe 0
    }

    "return 400 when Tax Code Form submission is empty" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-tax")

      val status = result.header.status
      status shouldBe 400
    }

    "return 303, when Tax Code Form submission, current list of aggregate and redirect to Is Over State Pension Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeStatePension)
      val formTax = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("K425")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-tax")

      val status = result.header.status
      val actualRedirectUri = redirectLocation(result).get

      val expectedRedirectUri = s"${baseURL}your-answers"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, when Tax Code Form submission, new list of aggregate and redirect to Is Over State Pension Page" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("K425")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-tax")

      val status = result.header.status
      val actualRedirectUri = redirectLocation(result).get

      val expectedRedirectUri = s"${baseURL}your-answers"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, when Tax Code form submission, the complete list of aggregate data and redirect to Summary Result Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeStatePensionSalary)
      val formTax = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("K425")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))

      val result = action(request.withSession("csrfToken" -> "someToken")
        .withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-tax")

      val status = result.header.status
      val actualRedirectUri = redirectLocation(result).get

      val expectedRedirectUri = s"${baseURL}your-answers"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }
  }

}
