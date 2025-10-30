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

import forms.{PlanFive, PlanFour, PlanTwo}
import models.{QuickCalcAggregateInput, ScottishRate}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.{FakeRequest, FakeRequest as application}
import play.api.test.Helpers.*
import services.QuickCalcCache
import setup.QuickCalcCacheSetup.*
import uk.gov.hmrc.http.HeaderNames
import play.api.test.CSRFTokenHelper.*

import scala.concurrent.Future
import scala.jdk.CollectionConverters.IteratorHasAsScala

class ShowSummarySpec extends PlaySpec with TryValues with ScalaFutures with IntegrationPatience with MockitoSugar {

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(
    app: Application,
    lang: String = "en"
  ): Messages =
    if (lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Summary Page" should {

    "return aggregate data of : Earning £20000 Yearly Salary, NOT (Over State Pension), Tax Code: S1150L and IS Scottish Tax Payer" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(cacheCompleteYearlyScottish))
      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustBe OK

        val responseBody = contentAsString(result)
        val parseHtml = Jsoup.parse(responseBody)

        val actualTable = parseHtml.getElementsByClass("govuk-summary-list__row")
        actualTable.size() mustBe 7

        actualTable
          .get(0)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe expectedYearlySalaryAnswer
        actualTable.get(1).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedStatePensionNO
        actualTable
          .get(2)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe "S1250L"
        actualTable
          .get(3)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe expectedScottishAnswerYes
      }
    }

    "return aggregate data of : Earning £50,000 Yearly Salary, NOT (Over State Pension), no Tax code and student plan type 5" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any()))
        .thenReturn(Future.successful(createStudentLoanTestData(testSalaryForStudentLoanPlan5(50000), PlanFive)))
      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustBe OK

        val responseBody = contentAsString(result)
        val parseHtml = Jsoup.parse(responseBody)

        val actualTable = parseHtml.getElementsByClass("govuk-summary-list__row")
        actualTable.size() mustBe 7

        actualTable
          .get(0)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe "£50,000 a year"
        actualTable.get(1).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedStatePensionNO
        actualTable
          .get(5)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe "Plan 5"
      }
    }

    "return aggregate data of : Earning £40 Daily Salary, 5 Days a Week, NOT (Over State Pension), Tax Code: 1150L and is NOT Scottish Tax Payer" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(cacheCompleteDaily))
      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustBe OK

        val responseBody = contentAsString(result)
        val parseHtml = Jsoup.parse(responseBody)

        val actualTable = parseHtml.getElementsByClass("govuk-summary-list__row")
        actualTable.size() mustBe 8

        actualTable
          .get(0)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe expectedDailySalaryAnswer
        actualTable
          .get(1)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe expectedDailyPeriodAnswer
        actualTable.get(2).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedStatePensionNO
        actualTable.get(3).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe "1250L"
        actualTable.get(4).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedScottishAnswer
      }
    }
    "return aggregate data of : Earning £8.5 Hourly Salary, YES (Over State Pension), Tax Code: 1150L and is NOT Scottish Tax Payer" in {
      val mockCache = mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(cacheCompleteHourly))
      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).value

        status(result) mustBe OK

        val responseBody = contentAsString(result)
        val parseHtml = Jsoup.parse(responseBody)

        val actualTable = parseHtml.getElementsByClass("govuk-summary-list__row")
        actualTable.size() mustBe 8

        actualTable
          .get(0)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe expectedHourlySalaryAnswer
        actualTable
          .get(1)
          .getElementsByClass("govuk-summary-list__value")
          .get(0)
          .text() mustBe expectedHourlyPeriodAnswer
        actualTable.get(2).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedStatePensionNO
        actualTable.get(3).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe "1250L"
        actualTable.get(4).getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedScottishAnswer
      }
    }
    "return 200 with correct welsh translation" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(cacheStatePensionSalary))
      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, "cy")

      running(application) {

        val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", "cy"))
          .withCSRFToken

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val header = doc.select(".govuk-header").text
        val betaBanner = doc.select(".govuk-phase-banner").text
        val heading = doc.select(".govuk-heading-xl").text
        val subHeading = doc.select(".govuk-heading-l").text
        val button = doc.select(".govuk-button").text
        val deskproLink = doc.select(".govuk-link")

        val rows = doc.select(".govuk-summary-list__row").iterator().asScala.toList
        rows.head.select(".govuk-summary-list__key").text() mustEqual messages(
          "quick_calc.you_have_told_us.a_year.label.new"
        )
        rows(1).select(".govuk-summary-list__key").text() mustEqual messages(
          "quick_calc.you_have_told_us.over_state_pension_age.label.new"
        )
        rows(2).select(".govuk-summary-list__key").text() mustEqual messages(
          "quick_calc.you_have_told_us.about_tax_code.label.new"
        )
        rows(3).select(".govuk-summary-list__key").text() mustEqual messages(
          "quick_calc.you_have_told_us.scottish_rate.label.new"
        )
        rows(4).select(".govuk-summary-list__key").text() mustEqual messages(
          "quick_calc.result.pension_contributions"
        )
        rows(5).select(".govuk-summary-list__key").text() mustEqual messages("quick_calc.result.student_loan")
        rows(6).select(".govuk-summary-list__key").text() mustEqual messages("quick_calc.result.postgraduate_loan")

        rows.head.select(".govuk-summary-list__value").text() must include(messages("quick_calc.salary.yearly.label"))
        rows(1).select(".govuk-summary-list__value").text() must include(
          messages("quick_calc.you_have_told_us.over_state_pension_age.yes")
        )
        rows(2).select(".govuk-summary-list__value").text() mustEqual messages("not_provided")
        rows(3).select(".govuk-summary-list__value").text() mustEqual messages("not_provided")
        rows(4).select(".govuk-summary-list__value").text() mustEqual messages("not_provided")
        rows(5).select(".govuk-summary-list__value").text() mustEqual messages("not_provided")
        rows(6).select(".govuk-summary-list__value").text() mustEqual messages("not_provided")

        rows.head.select(".govuk-summary-list__actions").text() must include(messages("quick_calc.you_have_told_us.edit"))
        rows(1).select(".govuk-summary-list__actions").text()   must include(messages("quick_calc.you_have_told_us.edit"))
        rows(2).select(".govuk-summary-list__actions").text() must include(
          messages("quick_calc.you_have_told_us.taxCode.add")
        )
        rows(3).select(".govuk-summary-list__actions").text() must include(
          messages("quick_calc.you_have_told_us.taxCode.add")
        )
        rows(4).select(".govuk-summary-list__actions").text() must include(
          messages("quick_calc.you_have_told_us.taxCode.add")
        )
        rows(5).select(".govuk-summary-list__actions").text() must include(
          messages("quick_calc.you_have_told_us.taxCode.add")
        )
        rows(6).select(".govuk-summary-list__actions").text() must include(
          messages("quick_calc.you_have_told_us.taxCode.add")
        )

        header     must include(messages("quick_calc.header.title"))
        betaBanner must include(messages("feedback.before"))
        betaBanner must include(messages("feedback.link"))
        betaBanner must include(messages("feedback.after"))
        heading mustEqual messages("quick_calc.you_have_told_us.header")
        subHeading must include(messages("quick_calc.you_have_told_us.subheading"))
        button mustEqual messages("calculate_take_home_pay")
        deskproLink.text must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        status(result) mustEqual OK
      }
    }

    "return 200 with correct  welsh translation" when {

      def checkWelshTranslation(inputAggregate: Option[QuickCalcAggregateInput], planTypeText: String) = {
        val mockCache = MockitoSugar.mock[QuickCalcCache]

        val expectedAggregate: QuickCalcAggregateInput = inputAggregate.get.copy(savedScottishRate = None)

        when(mockCache.fetchAndGetEntry()(any())).thenReturn(
          Future.successful(
            inputAggregate
              .map(
                _.copy(savedScottishRate = Some(ScottishRate(payScottishRate = Some(true))))
              )
          )
        )

        when(mockCache.save(ArgumentMatchers.eq(expectedAggregate))(any())).thenReturn(Future.successful(Done))

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application, "cy")

        running(application) {

          val request = FakeRequest(GET, routes.YouHaveToldUsNewController.summary().url)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCookies(Cookie("PLAY_LANG", "cy"))
            .withCSRFToken

          val result = route(application, request).get
          val doc: Document = Jsoup.parse(contentAsString(result))

          val rows = doc.select(".govuk-summary-list__row").iterator().asScala.toList
          rows(3).select(".govuk-summary-list__value").text() must include(
            messages("quick_calc.you_have_told_us.scottish_rate.yes")
          )
          rows(3).select(".govuk-summary-list__value").text() must include(
            messages("quick_calc.scottish_rate.payScottishRate.warning")
          )
          rows(4).select(".govuk-summary-list__value").text() must include(messages("label.a_month.value"))
          rows(5).select(".govuk-summary-list__value").text() mustEqual messages(planTypeText)
          rows(6).select(".govuk-summary-list__value").text() mustEqual messages("yes")

          def redirectUrl(row: Element) = row.select(".govuk-link").attr("href")

          redirectUrl(rows.head) mustEqual routes.SalaryController.showSalaryForm().toString
          redirectUrl(rows(1)) mustEqual routes.StatePensionController.showStatePensionForm().toString
          redirectUrl(rows(2)) mustEqual routes.TaxCodeController.showTaxCodeForm().toString
          redirectUrl(rows(3)) mustEqual routes.ScottishRateController.showScottishRateForm().toString
          redirectUrl(rows(4)) mustEqual routes.PensionContributionsPercentageController.showPensionContributionForm().toString
          redirectUrl(rows(5)) mustEqual routes.StudentLoanContributionsController.showStudentLoansForm().toString
          redirectUrl(rows(6)) mustEqual routes.PostgraduateController.showPostgraduateForm().toString

          status(result) mustEqual OK
        }
      }

      "plan type is 1" in {
        checkWelshTranslation(cacheCompleteYearly, "quick_calc.salary.studentLoan.plan1.text")
      }

      "plan type is 2" in {
        checkWelshTranslation(cacheCompleteYearly.map(_.copy(savedStudentLoanContributions = studentLoanContributions(PlanTwo))),
                              "quick_calc.salary.studentLoan.plan2.text"
                             )
      }

      "plan type is 4" in {
        checkWelshTranslation(cacheCompleteYearly.map(_.copy(savedStudentLoanContributions = studentLoanContributions(PlanFour))),
                              "quick_calc.salary.studentLoan.plan4.text"
                             )
      }
      "plan type is 5" in {
        checkWelshTranslation(cacheCompleteYearly.map(_.copy(savedStudentLoanContributions = studentLoanContributions(PlanFive))),
                              "quick_calc.salary.studentLoan.plan5.text"
                             )
      }

    }

  }
}
