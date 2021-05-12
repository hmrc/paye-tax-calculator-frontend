/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.routes
import models.{Salary, StatePension}
import play.api.i18n.Messages
import forms.YouHaveToldUs.salaryFormat
import setup.BaseSpec
import uk.gov.hmrc.play.test.UnitSpec

class YouHaveToldUsSpec extends BaseSpec with UnitSpec {

  "Converting Salary to YouHaveToldUsItem" in {
    val salaryUrl = routes.SalaryController.showSalaryForm().url
    val idSuffix  = "income"

    val yearlyLabel = "a_year"
    YouHaveToldUs(Salary(2, "a year", None)) shouldBe YouHaveToldUsItem("£2 a year", yearlyLabel, salaryUrl, idSuffix)

    val monthlyLabel = "a_month"
    YouHaveToldUs(Salary(3, "a month", None)) shouldBe YouHaveToldUsItem("£3 a month",
                                                                         monthlyLabel,
                                                                         salaryUrl,
                                                                         idSuffix)

    val weeklyLabel = "a_week"
    YouHaveToldUs(Salary(1, "a week", None)) shouldBe YouHaveToldUsItem("£1 a week", weeklyLabel, salaryUrl, idSuffix)

    val dailyLabel = "a_day"
    YouHaveToldUs(Salary(1, "a day", None)) shouldBe YouHaveToldUsItem("£1 a day", dailyLabel, salaryUrl, idSuffix)

    val hourlyLabel = "an_hour"
    YouHaveToldUs(Salary(2, "an hour", None)) shouldBe YouHaveToldUsItem("£2 an hour", hourlyLabel, salaryUrl, idSuffix)
  }

  "Converting OverStatePensionAge to YouHaveToldUsItem" in {
    val label    = "over_state_pension_age"
    val idSuffix = "pension-state"
    val url      = routes.StatePensionController.showStatePensionForm().url

    YouHaveToldUs(StatePension(true)) shouldBe YouHaveToldUsItem(
      Messages("quick_calc.you_have_told_us.over_state_pension_age.yes"),
      label,
      url,
      idSuffix
    )
    YouHaveToldUs(StatePension(false)) shouldBe YouHaveToldUsItem(
      Messages("quick_calc.you_have_told_us.over_state_pension_age.no"),
      label,
      url,
      idSuffix
    )
  }

  "getGoBackLink should give a go back url for the Check your answers page, " +
  "go back to scottish page if the user do not has a tax code, otherwise back to tax code" in {
    val itemsWithTaxCode = List(
      YouHaveToldUsItem("£400 an hour", "an_hour", "/estimate-paye-take-home-pay/your-pay", "income"),
      YouHaveToldUsItem("7", "an_hour_sub", "", "salary-period"),
      YouHaveToldUsItem("No", "over_state_pension_age", "/estimate-paye-take-home-pay/state-pension", "pension-state"),
      YouHaveToldUsItem("Yes (1150L)", "about_tax_code", "/estimate-paye-take-home-pay/tax-code", "tax-code")
    )

    val itemsWithOutTaxCode = List(
      YouHaveToldUsItem("£400 an hour", "an_hour", "/estimate-paye-take-home-pay/your-pay", "income"),
      YouHaveToldUsItem("7", "an_hour_sub", "", "salary-period"),
      YouHaveToldUsItem("No", "over_state_pension_age", "/estimate-paye-take-home-pay/state-pension", "pension-state"),
      YouHaveToldUsItem("No - we’ll use the default  (1150L)",
                        "about_tax_code",
                        "/estimate-paye-take-home-pay/tax-code",
                        "tax-code"),
      YouHaveToldUsItem("No", "scottish_rate", "/estimate-paye-take-home-pay/scottish-tax", "scottish_rate")
    )
    val taxCodeUrl  = "/estimate-paye-take-home-pay/tax-code"
    val scottishUrl = "/estimate-paye-take-home-pay/scottish-tax"
    taxCodeUrl  shouldBe YouHaveToldUs.getGoBackLink(itemsWithTaxCode)
    scottishUrl shouldBe YouHaveToldUs.getGoBackLink(itemsWithOutTaxCode)

  }

}
