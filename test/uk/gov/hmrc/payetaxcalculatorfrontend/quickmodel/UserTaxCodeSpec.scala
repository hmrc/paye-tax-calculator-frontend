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

package uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel

import play.api.data.Form
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.BaseSpec

class UserTaxCodeSpec extends BaseSpec {


  val userTaxCodeForm: Form[UserTaxCode] = UserTaxCode.form

  "The form function can create and verify the form for tax-code, and" should {
    "return true if all input is valid such as the tax-code etc, otherwise false." in {
      val form = userTaxCodeForm.bind(Map("hasTaxCode" -> "true", "taxCode" -> "K475"))
      form.hasErrors shouldBe false
    }
    "return error message if some input is invalid such as the tax-code etc." in {
      val form = userTaxCodeForm.bind(Map("hasTaxCode" -> "true", "taxCode" -> "foo"))
      val hasError = form.hasErrors
      val errorMessage = "Enter your current tax code as numbers and letters, making sure the number is between 0 and 9999"
      hasError shouldBe true
      errorMessage shouldBe form.errors.head.message
    }
  }

  "The checkUserSelection function" should {
    "not set yes or no options as checked, if user did not select neither" in {
      UserTaxCode.checkUserSelection(true, userTaxCodeForm) shouldBe ""
    }
    "set the yes option as checked, if user has checked yes in earlier operation" in {
      UserTaxCode.checkUserSelection(true, userTaxCodeForm.fill(UserTaxCode(true, Some(UserTaxCode.defaultUkTaxCode)))) shouldBe "checked"
    }
    "set the no option as checked, if user has checked no in earlier operation" in {
      UserTaxCode.checkUserSelection(false, userTaxCodeForm.fill(UserTaxCode(false, Some(UserTaxCode.defaultUkTaxCode)))) shouldBe "checked"
    }
  }

  "The hideTextField function" should {
    "keep the text field for tax code hidden when the user selects the no option" in {
      UserTaxCode.hideTextField(userTaxCodeForm.fill(UserTaxCode(false, Some(UserTaxCode.defaultUkTaxCode)))) shouldBe "hidden"
    }
    "show the text field when user selects the yes option" in {
      UserTaxCode.hideTextField(userTaxCodeForm.fill(UserTaxCode(true, Some(UserTaxCode.defaultUkTaxCode)))) shouldBe ""
    }
  }

  "Tax engine config" should {
    "be set only for current tax year" in {
      val config = UserTaxCode.taxConfig("1150L")
      import java.time.LocalDate.{of => ymd, _}

      if (now isBefore ymd(now.getYear,4,5)) {
        config.startDate.getYear shouldEqual (now.getYear - 1)
        config.endDate.getYear shouldEqual now.getYear
      } else {
        config.startDate.getYear shouldEqual now.getYear
        config.endDate.getYear shouldEqual (now.getYear + 1)
      }
    }
  }
}
