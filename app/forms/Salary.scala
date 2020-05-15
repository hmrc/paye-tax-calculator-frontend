/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.mappings.CustomFormatters
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n.Messages
import play.api.libs.json._

case class Salary(
  amount:       BigDecimal,
  period:       String,
  howManyAWeek: Option[Double])
case class Hours(
  amount:       Double,
  howManyAWeek: Double)
case class Days(
  amount:       Double,
  howManyAWeek: Double)
case class Detail(
  amount:       Int,
  howManyAWeek: Double,
  period:       String,
  urlForChange: String)

object Detail {
  implicit val format: OFormat[Detail] = Json.format[Detail]
}

object Salary {

  implicit val format: OFormat[Salary] = Json.format[Salary]

  def salaryBaseForm(implicit messages: Messages) = Form(
    mapping(
      "amount"       -> of(CustomFormatters.salaryValidation),
      "period"       -> of(CustomFormatters.requiredSalaryPeriodFormatter),
      "howManyAWeek" -> optional(of[Double])
    )(Salary.apply)(Salary.unapply)
  )

  def salaryInDaysForm(implicit messages: Messages) = Form(
    mapping(
      "amount"       -> of[Double],
      "howManyAWeek" -> of(CustomFormatters.dayValidation)
    )(Days.apply)(Days.unapply)
  )

  def salaryInHoursForm(implicit messages: Messages) = Form(
    mapping(
      a1 = "amount"       -> of[Double],
      a2 = "howManyAWeek" -> of(CustomFormatters.hoursValidation)
    )(Hours.apply)(Hours.unapply)
  )

  def salaryInPence(value: BigDecimal): Int =
    (value).toInt
}

object Days {
  implicit val daysFormat: OFormat[Days] = Json.format[Days]
}

object Hours {
  implicit val hoursFormat: OFormat[Hours] = Json.format[Hours]
}
