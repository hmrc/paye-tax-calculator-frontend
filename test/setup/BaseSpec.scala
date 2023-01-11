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
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Environment, Mode}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import services.Navigator
import uk.gov.hmrc.http.HeaderNames

class BaseSpec extends MockFactory with ScalaFutures with GuiceOneAppPerSuite with MetricClearSpec with Matchers {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure("metrics.enabled" -> "false")
    .build()

  val appInjector               = app.injector
  implicit val materializer     = appInjector.instanceOf[Materializer]
  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit val request = FakeRequest()
    .withHeaders(HeaderNames.xSessionId -> "test")

  implicit def appConfig:   AppConfig   = appInjector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi = appInjector.instanceOf[MessagesApi]
  val navigator:            Navigator   = appInjector.instanceOf[Navigator]
  implicit val messages:    Messages    = MessagesImpl(Lang("en-GB"), messagesApi)
}

import com.codahale.metrics.SharedMetricRegistries

trait MetricClearSpec {
  SharedMetricRegistries.clear()
}
