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

package forms

import models.ScottishResident
import org.scalatestplus.play.PlaySpec

class LiveInScotlandFormProviderSpec extends PlaySpec{

  val formProvider = new LiveInScotlandFormProvider()
  val form = formProvider()

  "LiveInScotlandFormProvider" should {
    "bind successfully when isScottishResident is true" in {
      val result = form.bind(Map("isScottishResident" -> "true"))

      result.errors mustBe empty
      result.value mustBe Some(ScottishResident(true))

    }

    "bind successfully when isScottishResident is false" in {
      val result = form.bind(Map("isScottishResident" -> "false"))

      result.errors mustBe empty
      result.value mustBe Some(ScottishResident(false))
    }

    "return error when isScottishResident is missing" in {
      val result = form.bind(Map.empty[String, String])

      result.errors must not be empty
      result.errors.head.key mustBe "isScottishResident"
    }

    "return error when isScottishResident is invalid" in {
      val result = form.bind(Map("isScottishResident" -> "invalid"))

      result.errors must not be empty
      result.errors.head.key mustBe "isScottishResident"
    }

    "fill the form correctly" in {
      val filledForm = form.fill(ScottishResident(true))

      filledForm.data must contain("isScottishResident" -> "true")
    }


  }

}
