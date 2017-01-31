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
