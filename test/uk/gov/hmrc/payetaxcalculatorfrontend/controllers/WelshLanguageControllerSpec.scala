/*
 * Copyright 2019 HM Revenue & Customs
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
// * Copyright 2017 HM Revenue & Customs
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
//package uk.gov.hmrc.payetaxcalculatorfrontend.controllers
//
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatestplus.play.OneAppPerSuite
//import play.api.Play
//import play.api.i18n.MessagesApi
//import play.api.mvc.Cookie
//import play.api.test.FakeRequest
//import play.api.test.Helpers._
//import uk.gov.hmrc.play.test.UnitSpec
//
//class WelshLanguageControllerSpec extends UnitSpec with OneAppPerSuite with ScalaFutures {
//
//  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
//
//  "WelshLanguageController" should {
//    "enable toggling the language in cookies from English to Welsh" in {
//      switchLang(targetLang= "welsh", expectedLangAfterChange = "cy")
//    }
//    "and back from Welsh to English"  in {
//      switchLang(targetLang = "english", expectedLangAfterChange = "en")
//    }
//    "default to English if some other value was passed" in {
//      switchLang(targetLang = "german", expectedLangAfterChange = "en")
//    }
//  }
//
//  def switchLang(targetLang: String, expectedLangAfterChange: String): Unit = {
//    val controller = new WelshLanguageController()
//
//    val result = controller.switchToLanguage(targetLang)(FakeRequest()).futureValue
//
//    cookies(result).get(Play.langCookieName) match {
//      case Some(c: Cookie) => c.value shouldBe expectedLangAfterChange
//      case _ => fail("PLAY_LANG cookie was not found.")
//    }
//  }
//}
