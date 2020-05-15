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

import org.jsoup.Jsoup
import org.scalatest.AppendedClues
import play.api.test.Helpers._
import setup.BaseSpec
import setup.QuickCalcCacheSetup._

class ShowScottishRateSpec extends BaseSpec with AppendedClues {

  "The show Scottish rate page" should {
    "return 200 - OK with existing aggregate data" in {
      val testController = new QuickCalcController(messagesApi, cacheReturnCompleteYearly, stubControllerComponents())

      val res = testController.showScottishRateForm()(request)
      status(res) shouldBe OK

      val html = Jsoup.parse(contentAsString(res))
      val noRadioOption = html.select("""input#scottish-rate-no""")
      noRadioOption.hasAttr("checked") shouldBe true withClue """"No" was not pre-selected"""
    }

    "return 303 See Other and redirect to the salary page with no aggregate data" in {
      val testController = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents())

      val res = testController.showScottishRateForm()(request)
      status(res) shouldBe SEE_OTHER
      redirectLocation(res) shouldBe Some(routes.QuickCalcController.showSalaryForm().url)
    }
  }
}
