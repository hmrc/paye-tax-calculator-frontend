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

package config.features

import setup.BaseSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.i18n.MessagesApi

class FeatureSpec extends BaseSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val features = new Features()(app.injector.instanceOf[Configuration])

  override def beforeEach(): Unit = {
    super.beforeEach()
    features.newScreenContentFeature(true)
  }

  "The new screen content feature" should {

    "return its current state" in {
      features.newScreenContentFeature() mustBe true
    }

    "switch to a new state" in {
      features.newScreenContentFeature(false)
      features.newScreenContentFeature() mustBe false
    }
  }

}
