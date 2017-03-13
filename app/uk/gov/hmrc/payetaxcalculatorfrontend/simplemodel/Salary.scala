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

package uk.gov.hmrc.payetaxcalculatorfrontend.simplemodel

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json._

case class Salary(value: BigDecimal, period: String)
case class Hours(value: BigDecimal, howManyAWeek: Int)
case class Days(value: BigDecimal, howManyAWeek: Int)

object Salary {

  implicit val format = Json.format[Salary]
  implicit val hoursFormat = Json.format[Hours]
  implicit val daysFormat = Json.format[Days]

  def salaryBaseForm(implicit messages: Messages) = Form(
    mapping(
      "value" -> bigDecimal(9,2),
      "period" -> nonEmptyText
    )(Salary.apply)(Salary.unapply)
  )

  def salaryInDaysForm(implicit messages: Messages) = Form(
    mapping(
      "value" -> bigDecimal(9,2),
      "period" -> number
    )(Days.apply)(Days.unapply)
  )

  def salaryInHoursForm(implicit messages: Messages) = Form(
    mapping(
      "value" -> bigDecimal(9,2),
      "period" -> number
    )(Hours.apply)(Hours.unapply)
  )

  def salaryInPence(value: BigDecimal): Int = {
    (value * 100).toInt
  }
}