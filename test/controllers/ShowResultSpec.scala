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

package controllers

import config.features.Features
import forms.TaxResult
import models.QuickCalcAggregateInput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, TryValues}
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.matchers.dsl.ResultOfGreaterThanComparison
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames
import utils.DefaultTaxCodeProvider
import views.html.pages.ResultView

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.concurrent.Future

class ShowResultSpec
    extends BaseSpec
    with TryValues
    with IntegrationPatience
    with CSRFTestHelper
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach {

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  private val features = new Features()(app.injector.instanceOf[Configuration])

  override def beforeEach(): Unit = {
    super.beforeEach()
    features.welshTranslationFeature(false)
  }

  def messagesThing(
    app:  Application,
    lang: String = "en"
  ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Result Page" should {

    def return200(
      fetchResponse:         Option[QuickCalcAggregateInput],
      yearlyEstimateAmount:  String,
      monthlyEstimateAmount: String,
      weeklyEstimateAmount:  String,
      lang:                  String = "en"
    ) = {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(fetchResponse)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        implicit val messages: Messages = messagesThing(application, lang)

        val request = FakeRequest(GET, routes.ShowResultsController.showResult.url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title          = doc.select(".govuk-heading-xl").text
        val tabList        = doc.select(".govuk-tabs__list").iterator().asScala.toList
        val list           = doc.select(".govuk-summary-list").iterator().asScala.toList
        val tab            = tabList(0).select(".govuk-tabs__list-item").text()
        val selectedTab    = tabList(0).select(".govuk-tabs__list-item--selected").text
        val panelTitle     = doc.select(".govuk-panel__title").text
        val list1          = list(0).select(".govuk-summary-list__row").text()
        val para           = doc.select(".govuk-body").text()
        val button         = doc.select(".govuk-button").text
        val sidebarHeader  = doc.select(".govuk-grid-column-one-third > .govuk-heading-s").text
        val sidebarBullets = doc.select(".govuk-list--bullet").get(0).text()
        val warningText    = doc.select(".govuk-warning-text").text()

        val isOverStatePension =
          fetchResponse.flatMap(_.savedIsOverStatePensionAge.map(_.overStatePensionAge)).getOrElse(false)

        val isOverThreshold: Boolean = fetchResponse.get.savedSalary.get.amount > 100000

        val hasTaxCode = fetchResponse.get.savedTaxCode.isDefined

        val hasPensionContri = fetchResponse.get.savedPensionContributions.isDefined

        val isScottishTaxRate = fetchResponse.flatMap(_.savedScottishRate.flatMap(_.payScottishRate)).getOrElse(false)

        status(result) mustEqual OK
        title must include(messages("quick_calc.result.you_take_home"))
        tab   must include(messages("quick_calc.result.tabLabels.yearly"))
        tab   must include(messages("quick_calc.result.tabLabels.monthly"))
        tab   must include(messages("quick_calc.result.tabLabels.weekly"))
        selectedTab mustEqual (messages("quick_calc.result.tabLabels.yearly"))
        panelTitle must include(s"$yearlyEstimateAmount ${messages("quick_calc.salary.yearly.label")}")
        panelTitle must include(s"$monthlyEstimateAmount ${messages("quick_calc.salary.monthly.label")}")
        panelTitle must include(s"$weeklyEstimateAmount ${messages("quick_calc.salary.weekly.label")}")
        list1      must include(messages("quick_calc.result.gross.income"))
        list1      must include(messages("quick_calc.result.personal_allowance"))
        list1      must include(messages("quick_calc.result.taxable_income"))
        list1      must include(messages("quick_calc.result.your_national_insurance"))
        list1      must include(messages("quick_calc.result.take_home_pay"))
        para       must include(messages("quick_calc.result.info.new"))
        para       must include(messages("clear_results"))
        button mustEqual (messages("update_answers"))
        sidebarHeader mustEqual (messages("quick_calc.result.sidebar.header"))
        sidebarBullets must include(messages("quick_calc.result.sidebar.one_job"))
        if (isOverStatePension)
          sidebarBullets    must include(messages("quick_calc.result.sidebar.over_state_pension_age"))
        else sidebarBullets must include(messages("quick_calc.result.sidebar.not_over_state_pension_age_b"))
        if (isOverThreshold && hasTaxCode)
          warningText must include(messages("quick_calc.result.disclaimer.reducedPersonal_allowance_a.new"))
        else if (isOverThreshold && !hasTaxCode) {
          if (lang == "en") sidebarBullets must include("(because you did not provide a tax code)")
          else sidebarBullets              must include("(oherwydd nad ydych wedi rhoi cod treth)")
        }

        if (isScottishTaxRate)
          sidebarBullets must include(messages("quick_calc.result.sidebar.pay_scottish_income_tax"))

        if (hasPensionContri)
          sidebarBullets must include(messages("quick_calc.result.sidebar.pension_exceed_annual_allowance_a"))

      }
    }

    "return 200, with estimated income tax when user's income is 20k and is over state pension " when {

      " form is in English" in {

        return200(cacheTaxCodeStatePensionSalary, "£18,501.80", "£1,541.82", "£355.81")
      }

      "form is in Welsh" in {

        return200(cacheTaxCodeStatePensionSalary, "£18,501.80", "£1,541.82", "£355.81", "cy")
      }
    }

    "return 200, with estimated income tax when user's income is 90k and is not over state pension" when {
      " form is in English" in {

        return200(cacheTaxCodeStatePensionSalaryLessThan100k, "£62,761", "£5,230.08", "£1,206.94")
      }

      "form is in Welsh" in {

        return200(cacheTaxCodeStatePensionSalaryLessThan100k, "£62,761", "£5,230.08", "£1,206.94", "cy")
      }

    }

    "return 200, with estimated income tax when user's income is 90k and using scottish tax rate" when {
      " form is in English" in {

        return200(cacheTaxCodeStatePensionSalaryLessThan100kWithScottishTax, "£59,915.14", "£4,992.93", "£1,152.22")
      }

      "form is in Welsh" in {

        return200(cacheTaxCodeStatePensionSalaryLessThan100kWithScottishTax, "£59,915.14", "£4,992.93", "£1,152.22", "cy")
      }

    }

    "return 200, with estimated income tax when user's income is more than 100k and is not over state pension" when {
      " form is in English" in {

        return200(cacheTaxCodeStatePensionSalaryMoreThan100k, "£68,562.74", "£5,713.56", "£1,318.51")
      }

      "form is in Welsh" in {

        return200(cacheTaxCodeStatePensionSalaryMoreThan100k, "£68,562.74", "£5,713.56", "£1,318.51", "cy")
      }

    }

    "return 200, with estimated income tax when user's income is more than 100k and is not over state pension and no tax code" when {
      " form is in English" in {

        return200(cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCode, "£68,562.14", "£5,713.51", "£1,318.50")
      }

      "form is in Welsh" in {

        return200(cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCode, "£68,562.14", "£5,713.51", "£1,318.50", "cy")
      }

    }
    "return 200, with estimated income tax when user's income has some pension contribution " when {
      " form is in English" in {

        return200(cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCodeWithPension, "£38,506.34", "£3,208.86", "£740.50")
      }

      "form is in Welsh" in {

        return200(cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCodeWithPension,
                  "£38,506.34",
                  "£3,208.86",
                  "£740.50",
                  "cy")
      }

    }
  }
}
