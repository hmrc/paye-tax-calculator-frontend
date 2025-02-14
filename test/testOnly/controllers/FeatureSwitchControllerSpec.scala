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

package testOnly.controllers

import config.AppConfig
import mocks.MockAppConfig
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import setup.BaseSpec
import testOnly.views.html.FeatureSwitchView

class FeatureSwitchControllerSpec extends BaseSpec {

  override implicit val mockAppConfig: AppConfig = new MockAppConfig(app.configuration)

  implicit val featureSwitch: FeatureSwitchView            = appInjector.instanceOf[FeatureSwitchView]
  override implicit val mcc:           MessagesControllerComponents = appInjector.instanceOf[MessagesControllerComponents]

  private lazy val target = new FeatureSwitchController

  "Calling the .featureSwitch action" should {

    lazy val result = target.featureSwitch(request.withCSRFToken)

    "return 200" in {
      await(result.map(_.header.status)) mustBe Status.OK
    }

    "return HTML" in {
      contentType(result) mustBe Some("text/html")
    }

    "return charset of utf-8" in {
      charset(result) mustBe Some("utf-8")
    }
  }

  "Calling the .submitFeatureSwitch action" should {

    lazy val result = target.submitFeatureSwitch(request.withCSRFToken)

    "return 303" in {
      await(result.map(_.header.status)) mustBe Status.SEE_OTHER
    }

    "redirect the user to the feature switch page" in {
      redirectLocation(result) mustBe Some(routes.FeatureSwitchController.featureSwitch.url)
    }
  }
}
