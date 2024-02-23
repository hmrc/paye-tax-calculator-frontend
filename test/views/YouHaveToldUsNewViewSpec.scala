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
import org.jsoup.nodes.Document
import setup.BaseSpec
import views.html.pages.YouHaveToldUsNewView

class YouHaveToldUsNewViewSpec extends BaseSpec{

  val youHaveToldUsNewView: YouHaveToldUsNewView = appInjector.instanceOf[YouHaveToldUsNewView]

  "Rendering the you have told us page" should {

    object Selectors {
      val pageHeading = "#main-content > div > div > h1"
      val firstSectionSubHeading = "#main-content > div > div > h3:nth-child(4)"
      val grossIncome = "#main-content > div > div > dl:nth-child(5) > div:nth-child(1) > dt"
      val salary = "#main-content > div > div > dl:nth-child(5) > div:nth-child(1) > dd.govuk-summary-list__value"
      val overStatePensionAge = "#main-content > div > div > dl:nth-child(5) > div:nth-child(2) > dt"
      val overStatePensionAgeValue = "#main-content > div > div > dl:nth-child(5) > div:nth-child(2) > dd.govuk-summary-list__value"
      val additionalQuestionsSubHeading = "#main-content > div > div > h3:nth-child(6)"
      val additionalQuestionsParagraph = "#main-content > div > div > p"
      val taxCodeSubheading = "#main-content > div > div > dl:nth-child(8) > div:nth-child(1) > dt"
      val taxCodeValue = "#main-content > div > div > dl:nth-child(8) > div:nth-child(1) > dd.govuk-summary-list__value"
      val scottishIncomeTax = "#main-content > div > div > dl:nth-child(8) > div:nth-child(2) > dt"
      val scottishIncomeTaxValue = "#main-content > div > div > dl:nth-child(8) > div:nth-child(2) > dd.govuk-summary-list__value"
      val calculateButton = "#button-get-results"
    }

    lazy val view = youHaveToldUsNewView(youHaveToldUsItems,additionalQuestionItem,Map.empty,taxCodeExists = true)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have thwe correct document title" in {
      document.title mustBe "Check your answers - PAYE Tax Calculator - GOV.UK"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) mustBe "Check your answers"
    }

    "have the correct subheading for the first section" in {
      elementText(Selectors.firstSectionSubHeading) mustBe "Your Income"
    }

    "have a subheading that says Gross Income" in {
      elementText(Selectors.grossIncome) mustBe "Gross income"
    }

    "have a salary of £2000" in {
      elementText(Selectors.salary) mustBe "£2000"
    }

    "have a subheading that says Over State Pension age" in {
      elementText(Selectors.overStatePensionAge) mustBe "Over State Pension age"
    }

    "have a value of Over State Pension age set to No" in {
      elementText(Selectors.overStatePensionAgeValue) mustBe "No"
    }

    "have an additional questions subheading" in {
      elementText(Selectors.additionalQuestionsSubHeading) mustBe "Additional questions (Optional)"
    }

    "have a paragraph text below the subheading" in {
      elementText(Selectors.additionalQuestionsParagraph) mustBe "Your results may be more accurate if you answer additional questions."
    }

    "have a subheading that says Tax code" in {
      elementText(Selectors.taxCodeSubheading) mustBe "Tax Code"
    }

    "have a value for tax code" in {
      elementText(Selectors.taxCodeValue) mustBe "1257L"
    }

    "have a subheading that says Scottish Income Tax" in {
      elementText(Selectors.scottishIncomeTax) mustBe "Scottish Income Tax"
    }

    "have a value of Scottish Income Tax set to No" in {
      elementText(Selectors.scottishIncomeTaxValue) mustBe "No"
    }

    "have a calculate take home pay button" in {
      elementText(Selectors.calculateButton) mustBe "Calculate take-home pay"
    }
  }
}
