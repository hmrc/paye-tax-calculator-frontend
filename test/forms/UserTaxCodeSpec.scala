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

package forms

import models.UserTaxCode
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.Form
import setup.BaseSpec

class UserTaxCodeSpec extends BaseSpec with AnyWordSpecLike {

  val userTaxCodeForm: Form[UserTaxCode] = new UserTaxCodeFormProvider().apply()

  "The form function can create and verify the form for tax-code, and" should {
    "return false if all input is valid such as the tax-code etc, otherwise false." in {
      val form = userTaxCodeForm.bind(Map("hasTaxCode" -> "true", "taxCode" -> "K475"))
      form.hasErrors mustBe false
    }
    "return false if all input is valid such hasTaxCode false." in {
      val form = userTaxCodeForm.bind(Map("hasTaxCode" -> "false"))
      form.hasErrors mustBe false
    }
    "return error message if some input is invalid such as the tax-code etc." in {
      val form     = userTaxCodeForm.bind(Map("hasTaxCode" -> "true", "taxCode" -> "foo"))
      val hasError = form.hasErrors
      val errorMessageKey =
        "quick_calc.about_tax_code.wrong_tax_code"
      hasError        mustBe true
      errorMessageKey mustBe form.errors.head.message
    }
  }
}
