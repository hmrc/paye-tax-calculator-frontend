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

import com.codahale.metrics.SharedMetricRegistries
import forms.PlanOne
import forms.forms.RemoveItemFormProvider
import models.{PensionContributions, PostgraduateLoanContributions, QuickCalcAggregateInput, StudentLoanContributions, UserTaxCode}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup._
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.Future

class RemoveItemControllerSpec
    extends BaseSpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with CSRFTestHelper {

  val taxCodeQueryParam      = "taxcode"
  val studentLoansQueryParam = "student-loans"
  val postgraduateLoansQueryParam = "postgraduate-loans"
  val pensionContributionQueryParam = "pension-contributions"
  val formProvider           = new RemoveItemFormProvider()
  val form: Form[Boolean] = formProvider(taxCodeQueryParam)

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messagesThing(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "The remove item page with taxcode set as the query param" should {
    "return 200 - Ok with an empty form" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      val request = FakeRequest(
        GET,
        routes.RemoveItemController.showRemoveItemForm(taxCodeQueryParam).url
      ).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

      val result = route(application, request).get

      status(result) mustEqual OK

    }

    "return 303 See Other and redirect to the salary page with no aggregate data" in {
      SharedMetricRegistries.clear()
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        None
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(
          GET,
          routes.RemoveItemController.showRemoveItemForm(taxCodeQueryParam).url
        ).withHeaders(HeaderNames.xSessionId -> "test")

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.SalaryController.showSalaryForm.url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }

    }
  }

  "Submit Remove Tax Code Controller" should {

    "return 303 See Other and redirect to the Check Your Answers Page if they remove there tax code" in {
      SharedMetricRegistries.clear()

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      val expectedAggregate: QuickCalcAggregateInput =
        cacheCompleteYearly.get.copy(savedTaxCode = Some(UserTaxCode(taxCode = None)))

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly.map(_.copy(savedTaxCode = Some(UserTaxCode(gaveUsTaxCode = true, taxCode = Some("1257L")))))
      )

      when(mockCache.save(ArgumentMatchers.eq(expectedAggregate))(any())) thenReturn Future
        .successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val formData = Map("removeItem" -> "true")
        val request = FakeRequest(
          POST,
          routes.RemoveItemController.submitRemoveItemForm(taxCodeQueryParam).url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken
        val result = route(application, request).get
        status(result) mustEqual SEE_OTHER
      }
    }

    "return 303 See Other and redirect to the Check Your Answers Page if they remove there pensionContributions" in {
      SharedMetricRegistries.clear()

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(savedPensionContributions = None)

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly.map(
          _.copy(savedPensionContributions = Some(
            PensionContributions(gaveUsPercentageAmount    = true,
                                 monthlyContributionAmount = Some(BigDecimal(12)),
                                 yearlyContributionAmount  = Some(BigDecimal(144)))
          )
          )
        )
      )

      when(mockCache.save(ArgumentMatchers.eq(expectedAggregate))(any())) thenReturn Future
        .successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val formData = Map("removeItem" -> "true")
        val request = FakeRequest(
          POST,
          routes.RemoveItemController.submitRemoveItemForm(pensionContributionQueryParam).url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken
        val result = route(application, request).get
        status(result) mustEqual SEE_OTHER
      }
    }

    "return 303 See Other and redirect to the Check Your Answers Page if they remove there studentLoanContributions" in {
      SharedMetricRegistries.clear()

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(savedStudentLoanContributions = None)

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly.map(
          _.copy(savedStudentLoanContributions = Some(
            StudentLoanContributions(studentLoanPlan = Some(PlanOne))
          )
          )
        )
      )

      when(mockCache.save(ArgumentMatchers.eq(expectedAggregate))(any())) thenReturn Future
        .successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val formData = Map("removeItem" -> "true")
        val request = FakeRequest(
          POST,
          routes.RemoveItemController.submitRemoveItemForm(studentLoansQueryParam).url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken
        val result = route(application, request).get
        status(result) mustEqual SEE_OTHER
      }
    }

    "return 303 See Other and redirect to the Check Your Answers Page if they remove there postgraduate loan contributions" in {
      SharedMetricRegistries.clear()

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(savedPostGraduateLoanContributions = None)

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly.map(
          _.copy(savedPostGraduateLoanContributions = Some(
            PostgraduateLoanContributions(hasPostgraduatePlan = Some(true))
          )
          )
        )
      )

      when(mockCache.save(ArgumentMatchers.eq(expectedAggregate))(any())) thenReturn Future
        .successful(Done)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {
        val formData = Map("removeItem" -> "true")
        val request = FakeRequest(
          POST,
          routes.RemoveItemController.submitRemoveItemForm(postgraduateLoansQueryParam).url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken
        val result = route(application, request).get
        status(result) mustEqual SEE_OTHER
      }
    }

    "return 400 for invalid form answer and current list of aggregate data" in {
      SharedMetricRegistries.clear()
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        cacheCompleteYearly
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      running(application) {

        val request = FakeRequest(
          POST,
          routes.RemoveItemController.submitRemoveItemForm(taxCodeQueryParam).url
        ).withFormUrlEncodedBody(form.data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader =
          parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true
        errorMessage.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true

      }

    }

    "return 400 for invalid form answer and empty list of aggregate data" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())) thenReturn Future.successful(
        None
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(
          POST,
          routes.RemoveItemController.submitRemoveItemForm(taxCodeQueryParam).url
        ).withFormUrlEncodedBody(form.data.toSeq: _*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual BAD_REQUEST

        val parseHtml = Jsoup.parse(contentAsString(result))

        val errorHeader =
          parseHtml.getElementsByClass("govuk-error-summary__title").text()
        val errorMessageLink = parseHtml.getElementsByClass("govuk-list govuk-error-summary__list").text()
        val errorMessage     = parseHtml.getElementsByClass("govuk-error-message").text()

        errorHeader mustEqual "There is a problem"
        errorMessageLink.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true
        errorMessage.contains(expectedInvalidRemoveTaxCodeAnswer) mustEqual true
      }
    }

  }

}
