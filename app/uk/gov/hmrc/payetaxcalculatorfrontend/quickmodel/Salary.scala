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

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json._

case class Salary(value: BigDecimal, period: String, howManyAWeek:Option[Int])
case class Hours(value: Int, howManyAWeek: Int)
case class Days(value: Int, howManyAWeek: Int)
case class Detail(howManyAWeek: Int, period: String)

object Detail {
  implicit val format = Json.format[Detail]
}

object Salary {

  implicit val format = Json.format[Salary]

  def salaryBaseForm(implicit messages: Messages) = Form(
    mapping(
      "value" -> of(CustomFormatters.salaryValidation),
      "period" -> nonEmptyText,
      "howManyAWeek" -> optional(number)
    )(Salary.apply)(Salary.unapply)
  )

  def salaryInDaysForm(implicit messages: Messages) = Form(
    mapping(
      "value" -> number,
      "howManyAWeek" -> of(CustomFormatters.dayValidation)
    )(Days.apply)(Days.unapply)
  )

  def salaryInHoursForm(implicit messages: Messages) = Form(
    mapping(
      "value" -> number,
      "howManyAWeek" -> of(CustomFormatters.hoursValidation)
    )(Hours.apply)(Hours.unapply)
  )

  def salaryInPence(value: BigDecimal): Int = {
    (value * 100).toInt
  }
}

object Days {
  implicit val daysFormat = Json.format[Days]
}

object Hours {
  implicit val hoursFormat = Json.format[Hours]
}