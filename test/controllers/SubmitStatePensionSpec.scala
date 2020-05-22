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

///*
// * Copyright 2020 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers
//
//import forms.StatePensionFormProvider
//import org.jsoup.Jsoup
//import play.api.test.Helpers._
//import setup.BaseSpec
//import setup.QuickCalcCacheSetup._
//import uk.gov.hmrc.http.SessionKeys
//
//class SubmitStatePensionSpec extends BaseSpec {
//
//  "Submit State Pension Form" should {
//    "return 400 for invalid form answer and current list of aggregate data" in {
//      val controller = new StatePensionController(messagesApi, cacheReturnTaxCode, stubControllerComponents(),navigator)
//      val formAge    = StatePensionFormProvider.form
//      val action     = await(controller.submitStatePensionForm())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formAge.data.toSeq: _*)
//          .withSession(SessionKeys.sessionId -> "test-StatePensionView")
//      )
//
//      val status = result.header.status
//
//      val responseBody = contentAsString(result)
//      val parseHtml    = Jsoup.parse(responseBody)
//
//      val actualHeaderErrorMessage = parseHtml.getElementById("over-state-pension-age-error-link").text()
//      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()
//
//      status                   shouldBe 400
//      actualHeaderErrorMessage shouldBe expectedInvalidStatePensionAnswerHeaderMessage
//      actualErrorMessage       shouldBe expectedYesNoAnswerErrorMessage
//    }
//
//    "return 400 for invalid form answer and empty list of aggregate data" in {
//      val controller = new StatePensionController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
//      val formAge    = StatePensionFormProvider.form
//      val action     = await(controller.submitStatePensionForm())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formAge.data.toSeq: _*)
//      ).withSession(SessionKeys.sessionId -> "test-StatePensionView")
//
//      val status = result.header.status
//
//      val responseBody = contentAsString(result)
//      val parseHtml    = Jsoup.parse(responseBody)
//
//      val actualHeaderErrorMessage = parseHtml.getElementById("over-state-pension-age-error-link").text()
//      val actualErrorMessage       = parseHtml.getElementsByClass("error-notification").text()
//
//      status                   shouldBe 400
//      actualHeaderErrorMessage shouldBe expectedInvalidStatePensionAnswerHeaderMessage
//      actualErrorMessage       shouldBe expectedYesNoAnswerErrorMessage
//    }
//
//    "return 303, with an answer \"No\" saved on existing list of aggregate data without Salary and redirect to Salary Page" in {
//      val controller = new StatePensionController(messagesApi, cacheReturnTaxCode, stubControllerComponents(),navigator)
//      val formAge    = StatePensionFormProvider.form.fill(StatePensionFormProvider(false))
//      val action     = await(controller.submitStatePensionForm())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formAge.data.toSeq: _*)
//      ).withSession(SessionKeys.sessionId -> "test-StatePensionView")
//
//      val status            = result.header.status
//      val actualRedirectUri = redirectLocation(result).get
//
//      val expectedRedirectUri = s"${baseURL}tax-code"
//
//      status            shouldBe 303
//      actualRedirectUri shouldBe expectedRedirectUri
//    }
//
//    "return 303, with an answer \"Yes\" for being Over 65 saved on a new list of aggregate data and redirect Salary Page" in {
//      val controller = new StatePensionController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
//      val formAge    = StatePensionFormProvider.form.fill(StatePensionFormProvider(true))
//      val action     = await(controller.submitStatePensionForm())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formAge.data.toSeq: _*)
//      ).withSession(SessionKeys.sessionId -> "test-StatePensionView")
//
//      val status            = result.header.status
//      val actualRedirectUri = redirectLocation(result).get
//
//      val expectedRedirectUri = s"${baseURL}tax-code"
//
//      status            shouldBe 303
//      actualRedirectUri shouldBe expectedRedirectUri
//    }
//
//    "return 303, with an answer \"No\" saved on the current list of aggregate data of all answered questions and redirect to Summary-Result" in {
//      val controller =
//        new StatePensionController(messagesApi, cacheReturnTaxCodeStatePensionSalary, stubControllerComponents(), navigator)
//      val formAge = StatePensionFormProvider.form.fill(StatePensionFormProvider(false))
//      val action  = await(controller.submitStatePensionForm())
//
//      val result = action(
//        request
//          .withFormUrlEncodedBody(formAge.data.toSeq: _*)
//      ).withSession(SessionKeys.sessionId -> "test-StatePensionView")
//
//      val status            = result.header.status
//      val actualRedirectUri = redirectLocation(result).get
//
//      val expectedRedirectUri = s"${baseURL}your-answers"
//
//      status            shouldBe 303
//      actualRedirectUri shouldBe expectedRedirectUri
//    }
//
//  }
//
//}
