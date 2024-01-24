/*
 * Copyright 2024 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import setup.BaseSpec
import views.html.pages.ResetView
import org.jsoup.nodes.Document

class ResetViewSpec  extends BaseSpec{

  val resetView: ResetView = appInjector.instanceOf[ResetView]

  "Rendering the reset page" should {

    object Selectors {
      val pageHeading = "#main-content > div > div > h1"
      val restartButton = "#restart-button"
    }

    lazy val view = resetView()
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have thwe correct document title" in {
      document.title mustBe "Your answers have been deleted - PAYE Tax Calculator - GOV.UK"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) mustBe "Your answers have been deleted"
    }

    "have a start again button on the page" in {
      elementText(Selectors.restartButton) mustBe "Start again"
    }

  }

}
