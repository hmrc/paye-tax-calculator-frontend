/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.model

import play.api.data.Form
import play.api.mvc.Flash
import uk.gov.hmrc.play.test.UnitSpec

class UserTaxCodeSpec extends UnitSpec {

  "The checkUserSelection function" should {

    "not set yes or no options as checked, if user did not select neither" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.checkUserSelection(true, userTaxCodeForm) shouldBe ""
    }

    "set the yes option as checked, if user has checked yes in earlier operation" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.checkUserSelection(true, userTaxCodeForm.fill(UserTaxCode(true, Some(UserTaxCode.defaultTaxCode)))) shouldBe "checked"
    }

    "set the no option as checked, if user has checked no in earlier operation" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.checkUserSelection(false, userTaxCodeForm.fill(UserTaxCode(false, Some(UserTaxCode.defaultTaxCode)))) shouldBe "checked"
    }

  }

  "The hideTextField function" should {
    "keep the text field for tax code hidden when the user selects the no option" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.hideTextField(userTaxCodeForm.fill(UserTaxCode(false, Some(UserTaxCode.defaultTaxCode)))) shouldBe "hidden"
    }

    "show the text field when user selects the yes option" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.hideTextField(userTaxCodeForm.fill(UserTaxCode(true, Some(UserTaxCode.defaultTaxCode))).withGlobalError("")) shouldBe ""
    }
  }
}
