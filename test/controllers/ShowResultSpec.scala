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
import forms.{PlanFour, PlanTwo, TaxResult, Yearly}
import models.{QuickCalcAggregateInput, Salary, StudentLoanContributions}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.{any, contains}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, TryValues}
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, *}
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup.*
import uk.gov.hmrc.http.HeaderNames

import java.time.LocalDate
import scala.jdk.CollectionConverters.*
import scala.concurrent.Future

class ShowResultSpec extends BaseSpec with TryValues with IntegrationPatience with CSRFTestHelper with GuiceOneAppPerSuite with BeforeAndAfterEach {

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  private val features = new Features()(app.injector.instanceOf[Configuration])

  override def beforeEach(): Unit = {
    super.beforeEach()
    features.welshTranslationFeature(false)
  }

  def messagesThing(
    app: Application,
    lang: String = "en"
  ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  val disableBeforeDate: LocalDate = LocalDate.of(2025, 4, 6)
  val currentDate: LocalDate = LocalDate.now()

  "Show Result Page" should {

    def return200(
      fetchResponse: Option[QuickCalcAggregateInput],
      yearlyEstimateAmount: String,
      monthlyEstimateAmount: String,
      weeklyEstimateAmount: String,
      lang: String = "en",
      taxableIncome: Option[String] = None
    ) = {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(fetchResponse))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        implicit val messages: Messages = messagesThing(application, lang)

        val request = FakeRequest(GET, routes.ShowResultsController.showResult().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title = doc.select(".govuk-heading-xl").text
        val tabList = doc.select(".govuk-tabs__list").iterator().asScala.toList
        val list = doc.select(".govuk-summary-list").iterator().asScala.toList
        val tab = tabList.head.select(".govuk-tabs__list-item").text()
        val selectedTab = tabList.head.select(".govuk-tabs__list-item--selected").text
        val panelTitle = doc.select(".govuk-panel__title").text
        val list1 = list.head.select(".govuk-summary-list__row").text()
        val para = doc.select(".govuk-body").text()
        val button = doc.select(".govuk-button").text
        val sidebarHeader = doc.select(".govuk-grid-column-one-third > .govuk-heading-s").text
        val sidebarBullets = doc.select(".govuk-list--bullet").get(0).text()
        val warningText = doc.select(".govuk-warning-text").text()
        val feedbackLink = doc.select(".govuk-link").attr("href")

        val listValues = doc
          .select(".govuk-summary-list")
          .iterator()
          .asScala
          .toList
          .head
          .select(".govuk-summary-list__value")
          .text()

        def getCalculation(str: String): Option[String] = {
          val words = str.trim.split("\\s+").filter(_.nonEmpty)
          if (words.length >= 3) Some(words(2)) else None
        }
        val fetchTaxableIncome = getCalculation(listValues)

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
        selectedTab mustEqual messages("quick_calc.result.tabLabels.yearly")
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
        button mustEqual messages("update_answers")
        sidebarHeader mustEqual messages("quick_calc.result.sidebar.header")
        sidebarBullets must include(messages("quick_calc.result.sidebar.one_job"))
        if (isOverStatePension)
          sidebarBullets    must include(messages("quick_calc.result.sidebar.over_state_pension_age"))
        else sidebarBullets must include(messages("quick_calc.result.sidebar.not_over_state_pension_age_b"))
        if (isOverThreshold && hasTaxCode)
          warningText must include(messages("quick_calc.result.disclaimer.reducedPersonal_allowance_a.new"))
        else if (isOverThreshold && !hasTaxCode) {
          if (lang == "en") sidebarBullets must include("(because you did not provide a tax code)")
          else {
            sidebarBullets must include("(oherwydd nad ydych wedi rhoi cod treth)")
          }
        }

        if (isScottishTaxRate)
          sidebarBullets must include(messages("quick_calc.result.sidebar.appliedScottishIncomeTaxRates"))

        if (hasPensionContri)
          sidebarBullets must include(messages("quick_calc.result.sidebar.pension_exceed_annual_allowance_a"))

        if (taxableIncome.isDefined)
          taxableIncome mustEqual fetchTaxableIncome
          
        feedbackLink  mustNot include("null")
        feedbackLink mustBe("http://localhost:9250/contact/beta-feedback-unauthenticated?service=PayeTaxCalculator")
      }
    }

    "return 200, with estimated income tax when user's income is 20k and is over state pension " when {

      "The income don't have scottish tax code" when {

        " form is in English" in {

          return200(
            fetchResponse         = cacheTaxCodeStatePensionSalary,
            yearlyEstimateAmount  = "£18,501.80",
            monthlyEstimateAmount = "£1,541.82",
            weeklyEstimateAmount  = "£355.81"
          )
        }

        "form is in Welsh" in {

          return200(
            fetchResponse         = cacheTaxCodeStatePensionSalary,
            yearlyEstimateAmount  = "£18,501.80",
            monthlyEstimateAmount = "£1,541.82",
            weeklyEstimateAmount  = "£355.81",
            lang                  = "cy"
          )
        }
      }

      if (currentDate.isBefore(disableBeforeDate)) {
        "The income has scottish tax code" when {

          " form is in English" in {

            return200(
              fetchResponse         = cacheTaxCodeStatePensionScottishSalary,
              yearlyEstimateAmount  = "£18,538.86",
              monthlyEstimateAmount = "£1,544.91",
              weeklyEstimateAmount  = "£356.52"
            )
          }

          "form is in Welsh" in {

            return200(
              fetchResponse         = cacheTaxCodeStatePensionScottishSalary,
              yearlyEstimateAmount  = "£18,538.86",
              monthlyEstimateAmount = "£1,544.91",
              weeklyEstimateAmount  = "£356.52",
              lang                  = "cy"
            )
          }
        }
      } else {
        "The income has scottish tax code" when {

          " form is in English" in {

            return200(
              fetchResponse         = cacheTaxCodeStatePensionScottishSalary,
              yearlyEstimateAmount  = "£18,544.07",
              monthlyEstimateAmount = "£1,545.34",
              weeklyEstimateAmount  = "£356.62"
            )
          }

          "form is in Welsh" in {

            return200(
              fetchResponse         = cacheTaxCodeStatePensionScottishSalary,
              yearlyEstimateAmount  = "£18,544.07",
              monthlyEstimateAmount = "£1,545.34",
              weeklyEstimateAmount  = "£356.62",
              lang                  = "cy"
            )
          }
        }
      }

    }
    "return 200, with estimated income tax when user's income is 90k and is not over state pension" when {
      " form is in English" in {

        return200(
          fetchResponse         = cacheTaxCodeStatePensionSalaryLessThan100k,
          yearlyEstimateAmount  = "£62,761",
          monthlyEstimateAmount = "£5,230.08",
          weeklyEstimateAmount  = "£1,206.94"
        )
      }

      "form is in Welsh" in {

        return200(
          fetchResponse         = cacheTaxCodeStatePensionSalaryLessThan100k,
          yearlyEstimateAmount  = "£62,761",
          monthlyEstimateAmount = "£5,230.08",
          weeklyEstimateAmount  = "£1,206.94",
          lang                  = "cy"
        )
      }

    }

    "return 200, with estimated income tax when user's income is 90k and using scottish tax rate" when {

      if (currentDate.isBefore(disableBeforeDate)) {
        "form is in English" in {

          return200(
            fetchResponse         = cacheTaxCodeStatePensionSalaryLessThan100kWithScottishTax,
            yearlyEstimateAmount  = "£59,915.14",
            monthlyEstimateAmount = "£4,992.93",
            weeklyEstimateAmount  = "£1,152.22"
          )
        }

        "form is in Welsh" in {

          return200(
            fetchResponse         = cacheTaxCodeStatePensionSalaryLessThan100kWithScottishTax,
            yearlyEstimateAmount  = "£59,915.14",
            monthlyEstimateAmount = "£4,992.93",
            weeklyEstimateAmount  = "£1,152.22",
            lang                  = "cy"
          )
        }
      } else {
        "form is in English" in {

          return200(
            fetchResponse         = cacheTaxCodeStatePensionSalaryLessThan100kWithScottishTax,
            yearlyEstimateAmount  = "£59,929.13",
            monthlyEstimateAmount = "£4,994.09",
            weeklyEstimateAmount  = "£1,152.48"
          )
        }

        "form is in Welsh" in {

          return200(
            fetchResponse         = cacheTaxCodeStatePensionSalaryLessThan100kWithScottishTax,
            yearlyEstimateAmount  = "£59,929.13",
            monthlyEstimateAmount = "£4,994.09",
            weeklyEstimateAmount  = "£1,152.48",
            lang                  = "cy"
          )
        }
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

    "return 200, with estimated income tax when user's income is more than £125141 and has scottish tax code" when {

      if (currentDate.isBefore(disableBeforeDate)) {
        " form is in English" in {

          return200(cacheTaxCodeStatePensionSalaryMoreThan125k, "£85,308.79", "£7,109.07", "£1,640.56")
        }

        "form is in Welsh" in {

          return200(cacheTaxCodeStatePensionSalaryMoreThan125k, "£85,308.79", "£7,109.07", "£1,640.56", "cy")
        }
      } else {
        " form is in English" in {

          return200(cacheTaxCodeStatePensionSalaryMoreThan125k, "£85,322.66", "£7,110.22", "£1,640.82")
        }

        "form is in Welsh" in {

          return200(cacheTaxCodeStatePensionSalaryMoreThan125k, "£85,322.66", "£7,110.22", "£1,640.82", "cy")
        }
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

        return200(
          fetchResponse         = cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCodeWithPension,
          yearlyEstimateAmount  = "£38,506.34",
          monthlyEstimateAmount = "£3,208.86",
          weeklyEstimateAmount  = "£740.50"
        )
      }

      "form is in Welsh" in {

        return200(cacheTaxCodeStatePensionSalaryMoreThan100kNoTaxCodeWithPension, "£38,506.34", "£3,208.86", "£740.50", "cy")
      }
    }

    "return 200, Taxable Income calculation is correct" when {

      "person is paid under personal allowance" in {
        return200(cacheTaxCodeStatePensionSalary.map(
                    _.copy(savedSalary = Some(Salary(7000, None, None, Yearly, None, None)))
                  ),
                  "£7,000",
                  "£583.33",
                  "£134.62",
                  "en",
                  Some("£0.00")
                 )
      }

      "person is paid equal to the personal allowance" in {
        return200(
          cacheTaxCodeStatePensionSalary.map(
            _.copy(savedSalary = Some(Salary(12570, None, None, Yearly, None, None)))
          ),
          "£12,557.80",
          "£1,046.48",
          "£241.50",
          "en",
          Some("£70.00")
        )
      }

      "person is paid under 100k" in {
        return200(cacheTaxCodeStatePensionSalaryLessThan100k, "£62,761", "£5,230.08", "£1,206.94", "en", Some("£77,430.00"))
      }

      "person is paid over 100k" in {
        return200(cacheTaxCodeStatePensionSalaryMoreThan100k, "£68,562.74", "£5,713.56", "£1,318.51", "en", Some("£87,433.00"))
      }
    }

    def studentLoanCalc(
      fetchResponse: Option[QuickCalcAggregateInput],
      contributions: Option[String] = None
    ) = {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(fetchResponse))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        implicit val messages: Messages = messagesThing(application)

        val request = FakeRequest(GET, routes.ShowResultsController.showResult().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val hasStudentLoan =
          fetchResponse.flatMap(_.savedStudentLoanContributions.flatMap(_.studentLoanPlan)).getOrElse(false)

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val listValues = doc
          .select(".govuk-summary-list")
          .iterator()
          .asScala
          .toList
          .head
          .select(".govuk-summary-list__value")
          .text()

        def getCalculation(str: String): Option[String] = {
          val words = str.trim.split("\\s+").filter(_.nonEmpty)
          if (words.length >= 3) Some(words(2)) else None
        }
        if (hasStudentLoan != false) {
          val loanContribution = getCalculation(listValues)
          loanContribution mustEqual contributions
        }
      }
    }

    "return 200, with correct student loan contribution having annual salary of 20k" when {

      if (currentDate.isBefore(disableBeforeDate)) {
        "Student has opted for plan 1" in {
          studentLoanCalc(cacheTaxCodeStatePensionSalaryLessThan100kWithStudentLoan, Some("£5,850.00"))
        }
        "Student has opted for plan 2" in {
          studentLoanCalc(
            cacheTaxCodeStatePensionSalaryLessThan100kWithStudentLoan.map(
              _.copy(savedStudentLoanContributions = Some(StudentLoanContributions(Some(PlanTwo))))
            ),
            Some("£5,643.00")
          )
        }
        "Student has opted for plan 4" in {
          studentLoanCalc(
            cacheTaxCodeStatePensionSalaryLessThan100kWithStudentLoan.map(
              _.copy(savedStudentLoanContributions = Some(StudentLoanContributions(Some(PlanFour))))
            ),
            Some("£5,274.00")
          )
        }

      } else {
        "Student has opted for plan 1" in {
          studentLoanCalc(cacheTaxCodeStatePensionSalaryLessThan100kWithStudentLoan, Some("£5,754.00"))
        }
        "Student has opted for plan 2" in {
          studentLoanCalc(
            cacheTaxCodeStatePensionSalaryLessThan100kWithStudentLoan.map(
              _.copy(savedStudentLoanContributions = Some(StudentLoanContributions(Some(PlanTwo))))
            ),
            Some("£5,537.00")
          )
        }
        "Student has opted for plan 4" in {
          studentLoanCalc(
            cacheTaxCodeStatePensionSalaryLessThan100kWithStudentLoan.map(
              _.copy(savedStudentLoanContributions = Some(StudentLoanContributions(Some(PlanFour))))
            ),
            Some("£5,152.00")
          )
        }
      }

    }
  }
}
