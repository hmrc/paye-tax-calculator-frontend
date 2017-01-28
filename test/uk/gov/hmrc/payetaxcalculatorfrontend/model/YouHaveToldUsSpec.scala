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

import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class YouHaveToldUsSpec extends UnitSpec with OneAppPerSuite {

  "Converting Salary to YouHaveToldUsItem" in {
    val salaryUrl =  "/salary" // TODO change when end-point exists

    val yearlyLabel = Messages("quick_calc.you_have_told_us.salary.yearly.label")
    YouHaveToldUs(Yearly(2)) shouldBe YouHaveToldUsItem("2", yearlyLabel, salaryUrl)

    val monthlyLabel = Messages("quick_calc.you_have_told_us.salary.monthly.label")
    YouHaveToldUs(Monthly(3)) shouldBe YouHaveToldUsItem("3", monthlyLabel, salaryUrl)

    val weeklyLabel = Messages("quick_calc.you_have_told_us.salary.weekly.label")
    YouHaveToldUs(Weekly(1)) shouldBe YouHaveToldUsItem("1", weeklyLabel, salaryUrl)

    val dailyLabel = Messages("quick_calc.you_have_told_us.salary.daily.label")
    YouHaveToldUs(Daily(1, 2)) shouldBe YouHaveToldUsItem("1", dailyLabel,salaryUrl)

    val hourlyLabel = Messages("quick_calc.you_have_told_us.salary.hourly.label")
    YouHaveToldUs(Hourly(2, 3)) shouldBe YouHaveToldUsItem("2", hourlyLabel, salaryUrl)
  }

  "Converting Over65 to YouHaveToldUsItem" in {
    val label = Messages("quick_calc.you_have_told_us.over_65.label")
    val url = "/over65" // TODO change when end-point exists

    YouHaveToldUs(Over65(true)) shouldBe YouHaveToldUsItem("Yes", label, url)
    YouHaveToldUs(Over65(false)) shouldBe YouHaveToldUsItem("No", label, url)
  }

}