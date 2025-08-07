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

import forms.ScottishRateFormProvider
import models.{QuickCalcAggregateInput, ScottishRate, UserTaxCode}
import org.apache.pekko.Done
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, Tag, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.QuickCalcCache
import setup.BaseSpec
import setup.QuickCalcCacheSetup.*
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.pages.ScottishRateView

import scala.concurrent.Future

class ScottishRateControllerSpec
    extends BaseSpec
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach
    with CSRFTestHelper
    with MockitoSugar
    with AnyWordSpecLike {

  val formProvider = new ScottishRateFormProvider()
  val form: Form[ScottishRate] = formProvider()

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

  "The show Scottish rate page" should {

    def return200(lang: String = "en") = {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(Future.successful(cacheCompleteYearly))

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, lang)

      val formFilled =
        form.fill(cacheCompleteYearly.get.savedScottishRate.get)
      running(application) {

        val request = FakeRequest(
          GET,
          routes.ScottishRateController.showScottishRateForm().url
        ).withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val result = route(application, request).get
        val doc: Document = Jsoup.parse(contentAsString(result))
        val title = doc.select(".govuk-fieldset__heading").text
        val radios = doc.select(".govuk-radios__item")
        val summary = doc.select(".govuk-details__summary").text
        val details = doc.select(".govuk-details__text").text
        val button = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link").text
        val backLink = doc.select(".govuk-back-link").text

        val view = application.injector.instanceOf[ScottishRateView]

        status(result) mustEqual OK
        title mustEqual messages("quick_calc.salary.question.scottish_income.new")
        radios.get(0).text mustEqual messages("quick_calc.you_have_told_us.scottish_rate.yes")
        radios.get(1).text mustEqual messages("quick_calc.you_have_told_us.scottish_rate.no")
        summary mustEqual messages("label.scottish-rate-details")
        details must include(messages("quick_calc.salary.question.scottish_income_info.new"))
        details must include(messages("quick_calc.salary.question.scottish_income_url_a"))
        details must include(messages("quick_calc.salary.question.scottish_income_url_b"))
        details must include(messages("quick_calc.salary.question.scottish_income_url_c"))
        button mustEqual messages("continue")
        if (lang == "cy")
          deskpro    must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        else deskpro must include("Is this page not working properly? (opens in new tab)")
        backLink mustEqual messages("back")

        removeCSRFTagValue(contentAsString(result)) mustEqual removeCSRFTagValue(
          view(
            formFilled,
            cacheCompleteYearly.get.youHaveToldUsItems()(messages, mockAppConfig)
          )(request, messagesThing(application, lang)).toString
        )
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }
    }

    "return 200 with existing aggregate data" when {
      "form is submitted in English" in {
        return200()
      }

      "form is submitted in Welsh" in {
        return200("cy")
      }
    }

    "return 303 See Other and redirect to the salary page with no aggregate data" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val request = FakeRequest(
          GET,
          routes.ScottishRateController.showScottishRateForm().url
        ).withHeaders(HeaderNames.xSessionId -> "test").withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[ScottishRateView]

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.SalaryController.showSalaryForm().url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())

      }

    }
  }

  "The submit Scottish rate page" should {

    "return 303  if the user does not select whether they pay the Scottish rate or not" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          cacheCompleteYearly
        )
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()
      implicit val messages: Messages = messagesThing(application)
      running(application) {

        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }

    "return 303 See Other and redirect to the Check Your Answers page if they submit valid form data" in {
      val mockCache = MockitoSugar.mock[QuickCalcCache]

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("payScottishRate" -> "true")

        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        val view = application.injector.instanceOf[ScottishRateView]

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual routes.YouHaveToldUsNewController.summary().url
        verify(mockCache, times(1)).fetchAndGetEntry()(any())
      }
    }

    "set the user's tax code to the 2018-19 default UK tax code if the user does not pay the Scottish rate" taggedAs Tag(
      "2018"
    ) in {
      val expectedAggregate: QuickCalcAggregateInput =
        cacheCompleteYearly.get.copy(
          savedScottishRate = Some(ScottishRate(payScottishRate = Some(false))),
          savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("1185L")))
        )

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
        fakeRequest,
        fakeRequest.session
      )

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      when(mockCache.save(expectedAggregate)(hc)).thenReturn(
        Future
          .successful(Done)
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .configure("dateOverride" -> "2018-04-06")
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("payScottishRate" -> "false")
        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }

    "set the user's tax code to the 2018-19 default Scottish tax code if the user pays the Scottish rate" taggedAs Tag(
      "2018"
    ) in {
      val expectedAggregate: QuickCalcAggregateInput =
        cacheCompleteYearly.get.copy(
          savedScottishRate = Some(ScottishRate(payScottishRate = Some(true))),
          savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1185L")))
        )

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
        fakeRequest,
        fakeRequest.session
      )

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      when(mockCache.save(expectedAggregate)(hc)).thenReturn(
        Future
          .successful(Done)
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .configure("dateOverride" -> "2018-04-06")
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("payScottishRate" -> "true")
        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }

    "set the user's tax code to the 2019-20 default UK tax code if the user does not pay the Scottish rate" taggedAs Tag(
      "2019"
    ) in {
      val expectedAggregate: QuickCalcAggregateInput =
        cacheCompleteYearly.get.copy(
          savedScottishRate = Some(ScottishRate(payScottishRate = Some(false))),
          savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("1250L")))
        )

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
        fakeRequest,
        fakeRequest.session
      )

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      when(mockCache.save(expectedAggregate)(hc)).thenReturn(
        Future
          .successful(Done)
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .configure("dateOverride" -> "2019-04-06")
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("payScottishRate" -> "false")
        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }

    "set the user's tax code to the 2019-20 default Scottish tax code if the user pays the Scottish rate" taggedAs Tag(
      "2019"
    ) in {
      val expectedAggregate: QuickCalcAggregateInput =
        cacheCompleteYearly.get.copy(
          savedScottishRate = Some(ScottishRate(payScottishRate = Some(true))),
          savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1250L")))
        )

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
        fakeRequest,
        fakeRequest.session
      )

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      when(mockCache.save(expectedAggregate)(hc)).thenReturn(
        Future
          .successful(Done)
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .configure("dateOverride" -> "2019-04-06")
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("payScottishRate" -> "true")
        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }

    "set the user's tax code to the 2020-21 default UK tax code if the user does not pay the Scottish rate" taggedAs Tag(
      "2020"
    ) in {
      val expectedAggregate: QuickCalcAggregateInput =
        cacheCompleteYearly.get.copy(
          savedScottishRate = Some(ScottishRate(payScottishRate = Some(false))),
          savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("1250L")))
        )

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
        fakeRequest,
        fakeRequest.session
      )

      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      when(mockCache.save(expectedAggregate)(hc)).thenReturn(
        Future
          .successful(Done)
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .configure("dateOverride" -> "2020-04-06")
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("payScottishRate" -> "false")
        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }

    "set the user's tax code to the 2020-21 default Scottish tax code if the user pays the Scottish rate" in {
      val expectedAggregate: QuickCalcAggregateInput =
        cacheCompleteYearly.get.copy(
          savedScottishRate = Some(ScottishRate(payScottishRate = Some(true))),
          savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1250L")))
        )

      val mockCache = MockitoSugar.mock[QuickCalcCache]

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
        fakeRequest,
        fakeRequest.session
      )
      when(mockCache.fetchAndGetEntry()(any())).thenReturn(
        Future.successful(
          None
        )
      )

      when(mockCache.save(expectedAggregate)(hc)).thenReturn(
        Future
          .successful(Done)
      )

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .configure("dateOverride" -> "2020-04-06")
        .build()

      implicit val messages: Messages = messagesThing(application)

      running(application) {

        val formData = Map("payScottishRate" -> "true")
        val request = FakeRequest(
          POST,
          routes.ScottishRateController.submitScottishRateForm().url
        ).withFormUrlEncodedBody(form.bind(formData).data.toSeq*)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCSRFToken

        val result = route(application, request).get

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
