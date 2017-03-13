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

import play.api.i18n.Messages
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.routes

case class YouHaveToldUsItem(value: String, label: String, url: String, idSuffix: String)

trait YouHaveToldUs[A] {
  def toYouHaveToldUsItem(t: A): YouHaveToldUsItem
}

object YouHaveToldUs {
  def apply[A : YouHaveToldUs](a: A) = implicitly[YouHaveToldUs[A]].toYouHaveToldUsItem(a)

  implicit def taxCodeFormat(implicit messages: Messages): YouHaveToldUs[UserTaxCode] = new YouHaveToldUs[UserTaxCode] {
    def toYouHaveToldUsItem(t: UserTaxCode): YouHaveToldUsItem = {
      val label = Messages("quick_calc.you_have_told_us.about_tax_code.label")
      val idSuffix = "tax-code"
      val url = routes.QuickCalcController.showTaxCodeForm().url
      YouHaveToldUsItem(t.taxCode.getOrElse(UserTaxCode.DEFAULT_TAX_CODE), label, url, idSuffix)
    }
  }

  implicit def overStatePensionAgeFormat(implicit messages: Messages) = new YouHaveToldUs[OverStatePensionAge] {
    def toYouHaveToldUsItem(overStatePensionAge: OverStatePensionAge): YouHaveToldUsItem = {
      val label = Messages("quick_calc.you_have_told_us.over_state_pension_age.label")
      val idSuffix = "pension-state"
      val url = routes.QuickCalcController.showAgeForm().url
      YouHaveToldUsItem(
        if(overStatePensionAge.value) Messages("quick_calc.you_have_told_us.over_state_pension_age.yes")
        else Messages("quick_calc.you_have_told_us.over_state_pension_age.no"), label, url, idSuffix)
    }
  }

  implicit def yearlyFormat(implicit m: Messages): YouHaveToldUs[Yearly] = formatForIndividualSalary[Yearly]
  implicit def monthlyFormat(implicit m: Messages): YouHaveToldUs[Monthly] = formatForIndividualSalary[Monthly]
  implicit def weeklyFormat(implicit m: Messages): YouHaveToldUs[Weekly] = formatForIndividualSalary[Weekly]
  implicit def dailyFormat(implicit m: Messages): YouHaveToldUs[Daily] = formatForIndividualSalary[Daily]
  implicit def hourlyFormat(implicit m: Messages): YouHaveToldUs[Hourly] = formatForIndividualSalary[Hourly]

  def formatForIndividualSalary[T <: Salary](implicit m: Messages): YouHaveToldUs[T] = new YouHaveToldUs[T] {
    def toYouHaveToldUsItem(salary: T) = salaryFormat.toYouHaveToldUsItem(salary)
  }

  implicit def salaryFormat(implicit messages: Messages) = new YouHaveToldUs[Salary] {
    def toYouHaveToldUsItem(s: Salary): YouHaveToldUsItem = {
      val url = routes.QuickCalcController.showSalaryForm().url
      def labelFor(s: String) = Messages(s"quick_calc.you_have_told_us.salary.$s.label")
      val idSuffix = "income"
      def asPounds(v: BigDecimal) = "£" + v
      s match {
        case Yearly(v) => YouHaveToldUsItem(asPounds(v), labelFor(Salary.YEARLY), url, idSuffix)
        case Monthly(v) => YouHaveToldUsItem(asPounds(v), labelFor(Salary.MONTHLY), url, idSuffix)
        case Weekly(v) => YouHaveToldUsItem(asPounds(v), labelFor(Salary.WEEKLY), url, idSuffix)
        case Daily(v, _) => YouHaveToldUsItem(asPounds(v), labelFor(Salary.DAILY), url, idSuffix)
        case Hourly(v, _) => YouHaveToldUsItem(asPounds(v), labelFor(Salary.HOURLY), url, idSuffix)
      }
    }
  }
}