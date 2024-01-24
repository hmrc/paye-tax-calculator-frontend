/*
 * Copyright 2023 HM Revenue & Customs
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

import akka.stream.Materializer
import config.AppConfig
import mocks.MockAppConfig
import org.jsoup.Jsoup
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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.Navigator
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.ExecutionContext

class BaseSpec
  extends MockFactory
    with ScalaFutures
    with GuiceOneAppPerSuite
    with MetricClearSpec with Matchers with MockitoSugar with AnyWordSpecLike {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure("metrics.enabled" -> "false", "auditing.enabled" -> "false")
    .build()

  val appInjector = app.injector
  implicit val materializer = appInjector.instanceOf[Materializer]
  implicit val executionContext = appInjector.instanceOf[ExecutionContext]

  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withHeaders(HeaderNames.xSessionId -> "test")

  implicit val mockAppConfig: AppConfig = new MockAppConfig(app.configuration)
  implicit val messagesApi: MessagesApi = appInjector.instanceOf[MessagesApi]
  val navigator: Navigator = appInjector.instanceOf[Navigator]
  implicit val messagesImplicit: Messages = MessagesImpl(Lang("en-GB"), messagesApi)

  def element(cssSelector: String)(implicit document: Document): Element = {
    val elements = document.select(cssSelector)

    if (elements.size == 0) {
      fail(s"No element exists with the selector '$cssSelector'")
    }

    document.select(cssSelector).first()
  }

  def elementText(selector: String)(implicit document: Document): String = {
    element(selector).text()
  }

}

  import com.codahale.metrics.SharedMetricRegistries
trait MetricClearSpec {
    SharedMetricRegistries.clear()
  }
