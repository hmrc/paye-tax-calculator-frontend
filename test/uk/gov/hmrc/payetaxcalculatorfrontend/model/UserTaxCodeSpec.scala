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
import uk.gov.hmrc.play.test.UnitSpec

class UserTaxCodeSpec extends UnitSpec {

  "UserTaxCode check user selection" should {

    "select nothing if user have not selected anything" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.recordCheck(true, userTaxCodeForm) shouldBe ""
    }

    "user has tax code" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.recordCheck(true, userTaxCodeForm.fill(UserTaxCode(true, Some(UserTaxCode.defaultTaxCode)))) shouldBe "checked"
    }

    "user has no tax code" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.recordCheck(false, userTaxCodeForm.fill(UserTaxCode(false, Some(UserTaxCode.defaultTaxCode)))) shouldBe "checked"
    }

    "some field should Hidden if user did not check has tax code" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.recordHidden(userTaxCodeForm.fill(UserTaxCode(false, Some(UserTaxCode.defaultTaxCode)))) shouldBe "hidden"
    }

    "some field should not Hidden if user did check has tax code" in {
      val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form
      UserTaxCode.recordHidden(userTaxCodeForm.fill(UserTaxCode(true, Some(UserTaxCode.defaultTaxCode)))) shouldBe ""
    }
  }
}
