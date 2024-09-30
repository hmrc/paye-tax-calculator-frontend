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

import controllers.routes
import models.{PayPeriodDetail, Salary, ScottishRate, StatePension, UserTaxCode}
import play.api.i18n.Messages

case class YouHaveToldUsItem(
  value:    String,
  label:    String,
  url:      String,
  idSuffix: String)

trait YouHaveToldUs[A] {
  def toYouHaveToldUsItem(t: A): YouHaveToldUsItem
}

object YouHaveToldUs {

  val SCOTTISH_RATE = "scottish_rate"

  def apply[A: YouHaveToldUs](a: A): YouHaveToldUsItem =
    implicitly[YouHaveToldUs[A]].toYouHaveToldUsItem(a)

  implicit def overStatePensionAgeFormat(implicit messages: Messages): YouHaveToldUs[StatePension] =
    new YouHaveToldUs[StatePension] {

      def toYouHaveToldUsItem(overStatePensionAge: StatePension): YouHaveToldUsItem = {
        val label    = "over_state_pension_age"
        val idSuffix = "pension-state"
        val url      = routes.StatePensionController.showStatePensionForm.url
        YouHaveToldUsItem(
          if (overStatePensionAge.overStatePensionAge)
            Messages("quick_calc.you_have_told_us.over_state_pension_age.yes")
          else
            Messages("quick_calc.you_have_told_us.over_state_pension_age.no"),
          label,
          url,
          idSuffix
        )
      }
    }

  implicit def salaryFormat(implicit messages: Messages) =
    new YouHaveToldUs[Salary] {

      def toYouHaveToldUsItem(s: Salary): YouHaveToldUsItem = {
        val url      = routes.SalaryController.showSalaryForm.url
        val idSuffix = "income"
        def asPounds(v: String) = "£" + v
        YouHaveToldUsItem(
          s"${asPounds(TaxResult.moneyFormatter(s.amount))}" + " " + Messages(
            s"label.${s.period.value.replace(" ", "_")}.value"
          ),
          s.period.value.replace(" ", "_"),
          url,
          idSuffix
        )
      }
    }

  implicit def salaryPeriodFormat(implicit messages: Messages) =
    new YouHaveToldUs[PayPeriodDetail] {
      val day:  String = messages("quick_calc.salary.daily.label")
      val hour: String = messages("quick_calc.salary.hourly.label")

      def toYouHaveToldUsItem(detail: PayPeriodDetail): YouHaveToldUsItem = {
        val label    = s"${detail.period.replace(" ", "_")}_sub"
        val idSuffix = "salary-period"
        val url = {
          detail.period match {
            case Daily.value =>
              routes.DaysPerWeekController
                .showDaysAWeek(
                  (detail.amount * 100.0).toInt
                )
                .url
            case Hourly.value =>
              routes.HoursPerWeekController
                .showHoursAWeek(
                  (detail.amount * 100.0).toInt
                )
                .url
          }
        }

        YouHaveToldUsItem(detail.howManyAWeek.toString, label, url, idSuffix)
      }
    }

  def getGoBackLink(items: List[YouHaveToldUsItem]): String =
    items.flatMap(y => if (y.label == SCOTTISH_RATE) y.url else "") match {
      case url if url.nonEmpty => url.mkString
      case _                   => routes.TaxCodeController.showTaxCodeForm.url
    }
}
