/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.setup

import akka.stream.Materializer
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.payetaxcalculatorfrontend.AppConfig
import uk.gov.hmrc.play.test.UnitSpec

class AppUnitGenerator extends UnitSpec with MockFactory with OneAppPerSuite {
  val appInjector = app.injector
  implicit val materializer = appInjector.instanceOf[Materializer]
  implicit val request = FakeRequest()
    .withHeaders(HeaderNames.xSessionId -> "test")

  implicit def appConfig: AppConfig = appInjector.instanceOf[AppConfig]
  implicit val messages = Messages(Lang.defaultLang, appInjector.instanceOf[MessagesApi])

}
