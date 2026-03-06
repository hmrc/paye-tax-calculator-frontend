/*
 * Copyright 2026 HM Revenue & Customs
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
import org.jsoup.nodes.Document
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.http.Status.OK
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Cookie}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import services.QuickCalcCache
import uk.gov.hmrc.http.HeaderNames
import views.html.pages.WinterFuelPaymentView

class WinterFuelPaymentsControllerSpec extends PlaySpec with TryValues with ScalaFutures with IntegrationPatience with MockitoSugar with CSRFTestHelper {

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type ]]

  def messagesThing(
    app: Application,
    lang: String = "en"
                  ): Messages =
    if(lang == "cy")
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest.withCookies(Cookie("PLAY_LANG", "cy")))
    else
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  "Show Winter Fuel Payment page" should {

    def showWinterFuelPaymentPage(lang: String = "en") = {
      val mockCache = mock[QuickCalcCache]

      val application = new GuiceApplicationBuilder()
        .overrides(bind[QuickCalcCache].toInstance(mockCache))
        .build()

      implicit val messages: Messages = messagesThing(application, lang)
      running(application) {

        val request = FakeRequest(GET, routes.WinterFuelPaymentsController.showWinterFuelPayments().url)
          .withHeaders(HeaderNames.xSessionId -> "test")
          .withCookies(Cookie("PLAY_LANG", lang))
          .withCSRFToken

        val view = application.injector.instanceOf[WinterFuelPaymentView]
        val result = route(application, request).value
        val doc: Document = Jsoup.parse(contentAsString(result))
        val header = doc.select(".govuk-header").text
        val betaBanner = doc.select(".govuk-phase-banner").text
        val heading = doc.select(".govuk-heading-xl").text
        val content = doc.select(".govuk-body").text
        val warningText = doc.select(".govuk-warning-text__text").text
        val button = doc.select(".govuk-button").text
        val deskpro = doc.select(".govuk-link")

        status(result) mustEqual OK
        header must include(messages("quick_calc.header.title"))
        betaBanner must include(messages("feedback.before"))
        betaBanner must include(messages("feedback.link"))
        betaBanner must include(messages("feedback.after"))
        heading mustEqual messages("quick_calc.wfp_heading")
        button mustEqual messages("continue")
        warningText must include(messages("quick_calc.wfp_warning"))
        content must include(messages("quick_calc.wfp_subheading1"))
        content must include(messages("quick_calc.wfp_subheading2"))
        content must include(messages("quick_calc.wfp_subheading3"))
        content must include(messages("quick_calc.wfp_subheading4"))
        if (lang == "cy")
          deskpro.text() must include("A yw’r dudalen hon yn gweithio’n iawn? (yn agor tab newydd)")
        else deskpro.text() must include("Is this page not working properly? (opens in new tab)")
      }
    }

    "return WFP page" when {
      "existing aggregate data in form" when {
        "submitted in english" in {
          showWinterFuelPaymentPage()
        }

        "submitted in welsh" in {
          showWinterFuelPaymentPage("cy")
        }
      }
    }

    "Continue button should redirect to your-answers" when {
      "continue button is clicked" in {
        val mockCache = mock[QuickCalcCache]

        val application = new GuiceApplicationBuilder()
          .overrides(bind[QuickCalcCache].toInstance(mockCache))
          .build()

        implicit val messages: Messages = messagesThing(application)
        running(application) {

          val request = FakeRequest(GET, routes.WinterFuelPaymentsController.showWinterFuelPayments().url)
            .withHeaders(HeaderNames.xSessionId -> "test")
            .withCSRFToken

          val result = route(application, request).value
          val doc: Document = Jsoup.parse(contentAsString(result))

          doc.select("form")
            .attr("action") mustEqual routes.YouHaveToldUsNewController.summary().url
        }
      }

    }

  }


}
