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

package setup

import config.AppConfig
import forms.{AdditionalQuestionItem, YouHaveToldUsItem}
import mocks.MockAppConfig
import org.apache.pekko.stream.Materializer
import org.jsoup.nodes.{Document, Element}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Environment, Mode}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import services.Navigator
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.ExecutionContext

class BaseSpec
    extends AnyWordSpecLike
    with MockFactory
    with ScalaFutures
    with GuiceOneAppPerSuite
    with MetricClearSpec
    with Matchers
    with MockitoSugar {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure("metrics.enabled" -> "false", "auditing.enabled" -> "false")
    .build()

  val appInjector: Injector = app.injector
  implicit val materializer: Materializer = appInjector.instanceOf[Materializer]
  implicit val executionContext: ExecutionContext = appInjector.instanceOf[ExecutionContext]
  val mcc: MessagesControllerComponents = appInjector.instanceOf[MessagesControllerComponents]


  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withHeaders(HeaderNames.xSessionId -> "test")

  implicit val mockAppConfig:    AppConfig   = new MockAppConfig(app.configuration)
  implicit val messagesApi:      MessagesApi = appInjector.instanceOf[MessagesApi]
  val navigator:                 Navigator   = appInjector.instanceOf[Navigator]
  implicit val messagesImplicit: Messages    = MessagesImpl(Lang("en-GB"), messagesApi)

  def element(cssSelector: String)(implicit document: Document): Element = {
    val elements = document.select(cssSelector)

    if (elements.size == 0) {
      fail(s"No element exists with the selector '$cssSelector'")
    }

    document.select(cssSelector).first()
  }

  def elementText(selector: String)(implicit document: Document): String =
    element(selector).text()

  val youHaveToldUsItems: List[YouHaveToldUsItem] =
    List(
      YouHaveToldUsItem("£2000", "a_year", controllers.routes.SalaryController.showSalaryForm.url, "income"),
      YouHaveToldUsItem("No",
                        "over_state_pension_age",
                        controllers.routes.StatePensionController.showStatePensionForm.url,
                        "pension-state")
    )

  val additionalQuestionItem: List[AdditionalQuestionItem] =
    List(
      AdditionalQuestionItem("1257L",
                             "about_tax_code",
                             controllers.routes.TaxCodeController.showTaxCodeForm.url,
                             "tax-code"),
      AdditionalQuestionItem("No",
                             "scottish_rate",
                             controllers.routes.ScottishRateController.showScottishRateForm.url,
                             "scottish_rate"),
      AdditionalQuestionItem("43% a month",
        "about_pensions_contributions",
        controllers.routes.PensionContributionsPercentageController.showPensionContributionForm.url,
        "pension-contributions"),
      AdditionalQuestionItem("Plan 1", "about_student_loan_contribution", controllers.routes.StudentLoanContributionsController.showStudentLoansForm.url,"student_loan_contribution"),
      AdditionalQuestionItem("Yes", "about_post_graduate_loan_contribution", controllers.routes.PostgraduateController.showPostgraduateForm.url,"post_graduate_loan_contribution")
    )
}

import com.codahale.metrics.SharedMetricRegistries

trait MetricClearSpec {
  SharedMetricRegistries.clear()
}
