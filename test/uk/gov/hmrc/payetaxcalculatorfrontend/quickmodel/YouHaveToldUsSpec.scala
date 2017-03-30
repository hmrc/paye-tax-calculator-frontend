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

package uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel

import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.routes
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.YouHaveToldUs.salaryFormat

class YouHaveToldUsSpec extends UnitSpec with OneAppPerSuite {

  "Converting Salary to YouHaveToldUsItem" in {
    val salaryUrl =  routes.QuickCalcController.showSalaryForm().url
    val idSuffix = "income"

    val yearlyLabel = Messages("quick_calc.salary.yearly")
    YouHaveToldUs(Salary(2, "a year", None)) shouldBe YouHaveToldUsItem("£2", yearlyLabel, salaryUrl, idSuffix)

    val monthlyLabel = Messages("quick_calc.salary.monthly")
    YouHaveToldUs(Salary(3, "a month", None)) shouldBe YouHaveToldUsItem("£3", monthlyLabel, salaryUrl, idSuffix)

    val weeklyLabel = Messages("quick_calc.salary.weekly")
    YouHaveToldUs(Salary(1, "a week", None)) shouldBe YouHaveToldUsItem("£1", weeklyLabel, salaryUrl, idSuffix)

    val dailyLabel = Messages("quick_calc.salary.daily")
    YouHaveToldUs(Salary(1, "a day", None)) shouldBe YouHaveToldUsItem("£1", dailyLabel,salaryUrl, idSuffix)

    val hourlyLabel = Messages("quick_calc.salary.hourly")
    YouHaveToldUs(Salary(2, "an hour", None)) shouldBe YouHaveToldUsItem("£2", hourlyLabel, salaryUrl, idSuffix)
  }

  "Converting OverStatePensionAge to YouHaveToldUsItem" in {
    val label = "over_state_pension_age"
    val idSuffix = "pension-state"
    val url = routes.QuickCalcController.showStatePensionForm().url

    YouHaveToldUs(OverStatePensionAge(true)) shouldBe YouHaveToldUsItem(Messages("quick_calc.you_have_told_us.over_state_pension_age.yes"), label, url, idSuffix)
    YouHaveToldUs(OverStatePensionAge(false)) shouldBe YouHaveToldUsItem(Messages("quick_calc.you_have_told_us.over_state_pension_age.no"), label, url, idSuffix)
  }

}