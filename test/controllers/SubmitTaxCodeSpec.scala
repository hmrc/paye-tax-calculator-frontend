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

import forms.UserTaxCode
import org.jsoup.Jsoup
import play.api.test.Helpers._
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.SessionKeys

class SubmitTaxCodeSpec extends BaseSpec {

  "Submit Tax Code Form" should {
    "return 400, with aggregate data and an error message for invalid Tax Code" in {
      val controller = new QuickCalcController(messagesApi, cacheReturnTaxCode, stubControllerComponents(),navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("110")))
      val action     = await(controller.submitTaxCodeForm())
      val result = action(request.withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status       = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml    = Jsoup.parse(responseBody)

      val actualHeaderErrorMessage = parseHtml.getElementById("tax-code-error-link").text()
      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()

      status                   shouldBe 400
      actualErrorMessage       shouldBe expectedInvalidTaxCodeErrorMessage
      actualHeaderErrorMessage shouldBe expectedInvalidTaxCodeHeaderMessage
    }

    "return 400, with no aggregate data and an error message for invalid Tax Code" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("110")))
      val action     = await(controller.submitTaxCodeForm())
      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status       = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml    = Jsoup.parse(responseBody)

      val actualHeaderErrorMessage = parseHtml.getElementById("tax-code-error-link").text()
      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()

      status                   shouldBe 400
      actualErrorMessage       shouldBe expectedInvalidTaxCodeErrorMessage
      actualHeaderErrorMessage shouldBe expectedInvalidTaxCodeHeaderMessage
    }

    "return 400, with no aggregate data and an error message for invalid Tax Code Prefix" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("OO9999")))
      val action     = await(controller.submitTaxCodeForm())
      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status       = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml    = Jsoup.parse(responseBody)

      val actualHeaderErrorMessage = parseHtml.getElementById("tax-code-error-link").text()
      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()

      status                   shouldBe 400
      actualErrorMessage       shouldBe expectedPrefixTaxCodeErrorMessage
      actualHeaderErrorMessage shouldBe expectedInvalidTaxCodeHeaderMessage
    }

    "return 400, with no aggregate data and an error message for invalid Tax Code Suffix" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("9999R")))
      val action     = await(controller.submitTaxCodeForm())
      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status       = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml    = Jsoup.parse(responseBody)

      val actualHeaderErrorMessage = parseHtml.getElementById("tax-code-error-link").text()
      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()

      status                   shouldBe 400
      actualErrorMessage       shouldBe expectedSuffixTaxCodeErrorMessage
      actualHeaderErrorMessage shouldBe expectedInvalidTaxCodeHeaderMessage

    }

    "return 400, with no aggregate data and an error message when user selects \"Yes\" but did not enter Tax code" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, None))
      val action     = await(controller.submitTaxCodeForm())
      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status       = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml    = Jsoup.parse(responseBody)

      val actualHeaderErrorMessage = parseHtml.getElementById("tax-code-error-link").text()
      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()

      status                   shouldBe 400
      actualErrorMessage       shouldBe expectedEmptyTaxCodeErrorMessage
      actualHeaderErrorMessage shouldBe expectedEmptyTaxCodeHeaderMessage
    }

    "return 400, with no aggregate data and an error message when user selects \"Yes\" but Tax Code entered is 99999L" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("99999L")))
      val action     = await(controller.submitTaxCodeForm())
      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
      val status       = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml    = Jsoup.parse(responseBody)

      val actualHeaderErrorMessage = parseHtml.getElementById("tax-code-error-link").text()
      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()

      status                   shouldBe 400
      actualErrorMessage       shouldBe expectedWrongNumberTaxCodeErrorMessage
      actualHeaderErrorMessage shouldBe expectedInvalidTaxCodeHeaderMessage
    }

    "return 400, with no aggregate data when Tax Code Form submission is empty" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form
      val action     = await(controller.submitTaxCodeForm())

      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(SessionKeys.sessionId -> "test-tax")

      val status       = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml    = Jsoup.parse(responseBody)

      val actualHeaderErrorMessage = parseHtml.getElementById("has-tax-code-error-link").text()
      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()

      status                   shouldBe 400
      actualErrorMessage       shouldBe expectedYesNoAnswerErrorMessage
      actualHeaderErrorMessage shouldBe expectedNotAnsweredTaxCodeHeaderMessage
    }

    "return 303, with current aggregate data and redirect to Is Over State Pension Page" in {
      val controller = new QuickCalcController(messagesApi, cacheReturnTaxCodeStatePension, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("K425")))
      val action     = await(controller.submitTaxCodeForm())

      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(SessionKeys.sessionId -> "test-tax")

      val status            = result.header.status
      val actualRedirectUri = redirectLocation(result).get

      val expectedRedirectUri = s"${baseURL}your-answers"

      status            shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, with no aggregate data and redirect to Is Over State Pension Page" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val formTax    = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("K425")))
      val action     = await(controller.submitTaxCodeForm())

      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(SessionKeys.sessionId -> "test-tax")

      val status            = result.header.status
      val actualRedirectUri = redirectLocation(result).get

      val expectedRedirectUri = s"${baseURL}your-answers"

      status            shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, with current aggregate data and redirect to Summary Result Page" in {
      val controller =
        new QuickCalcController(messagesApi, cacheReturnTaxCodeStatePensionSalary, stubControllerComponents(), navigator)
      val formTax = UserTaxCode.form.fill(UserTaxCode(gaveUsTaxCode = true, Some("K425")))
      val action  = await(controller.submitTaxCodeForm())

      val result = action(
        request
          .withFormUrlEncodedBody(formTax.data.toSeq: _*)
      ).withSession(SessionKeys.sessionId -> "test-tax")

      val status            = result.header.status
      val actualRedirectUri = redirectLocation(result).get

      val expectedRedirectUri = s"${baseURL}your-answers"

      status            shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }
  }

}
