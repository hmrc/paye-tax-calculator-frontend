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
import uk.gov.voa.play.form.ConditionalMappings._
import uk.gov.hmrc.payetaxcalculatorfrontend.model.CustomFormatters._
import views.html.helper.FieldElements


sealed trait Salary

case class YearlyAmount(value: BigDecimal) extends Salary

case class WeeklyAmount(value: BigDecimal) extends Salary

case class MonthlyAmount(value: BigDecimal) extends Salary

case class DailyAmount(value: BigDecimal) extends Salary

case class HourlyAmount(value: BigDecimal) extends Salary

sealed trait TimeRate

case class Hours(value: Int) extends TimeRate

case class Days(value: Int) extends TimeRate

object Salary {
  val YEARLY = "yearly"
  val WEEKLY = "weekly"
  val MONTHLY = "monthly"
  val DAILY = "daily"
  val HOURLY = "hourly"

  def addTypeInfo(s: String): JsObject => JsObject = _ ++ Json.obj("type" -> s)

  val yearlyWrites = Json.writes[YearlyAmount].transform(addTypeInfo(YEARLY))
  val monthlyWrites = Json.writes[MonthlyAmount].transform(addTypeInfo(MONTHLY))
  val weeklyWrites = Json.writes[WeeklyAmount].transform(addTypeInfo(WEEKLY))
  val dailyWrites = Json.writes[DailyAmount].transform(addTypeInfo(DAILY))
  val hourlyWrites = Json.writes[HourlyAmount].transform(addTypeInfo(HOURLY))

  implicit val salaryWrites = new Writes[Salary] {
    def writes(o: Salary): JsValue = o match {
      case s: YearlyAmount => yearlyWrites.writes(s)
      case s: MonthlyAmount => monthlyWrites.writes(s)
      case s: WeeklyAmount => weeklyWrites.writes(s)
      case s: DailyAmount => dailyWrites.writes(s)
      case s: HourlyAmount => hourlyWrites.writes(s)
    }
  }

  implicit val reads = new Reads[Salary] {
    def reads(json: JsValue): JsResult[Salary] = {
      (json \ "type").asOpt[String].map {
        case YEARLY => Json.reads[YearlyAmount].reads(json)
        case WEEKLY => Json.reads[WeeklyAmount].reads(json)
        case MONTHLY => Json.reads[MonthlyAmount].reads(json)
        case DAILY => Json.reads[DailyAmount].reads(json)
        case HOURLY => Json.reads[HourlyAmount].reads(json)
      }.getOrElse(JsError("unable to parse as salary, type not found"))
    }
  }

  implicit val hourlyFormat = Json.format[Hours]
  implicit val dailyFormat = Json.format[Days]

  def formToSalary(salaryType: String,
                   amountYearly: Option[BigDecimal],
                   amountMonthly: Option[BigDecimal],
                   amountWeekly: Option[BigDecimal],
                   amountDaily: Option[BigDecimal],
                   amountHourly: Option[BigDecimal]): Salary = {
    salaryType match {
      case Salary.YEARLY => YearlyAmount(amountYearly.get)
      case Salary.MONTHLY => MonthlyAmount(amountMonthly.get)
      case Salary.WEEKLY => WeeklyAmount(amountWeekly.get)
      case Salary.DAILY => DailyAmount(amountDaily.get)
      case Salary.HOURLY => HourlyAmount(amountHourly.get)
    }
  }

  def salaryToForm(salary: Salary) = {
    val noneY: Option[BigDecimal] = None
    val noneM: Option[BigDecimal] = None
    val noneW: Option[BigDecimal] = None
    val noneD: Option[BigDecimal] = None
    val noneH: Option[BigDecimal] = None
    salary match {
      case s: YearlyAmount => Some((Salary.YEARLY, Some(s.value), noneM, noneW, noneD, noneH))
      case s: MonthlyAmount => Some((Salary.MONTHLY, noneY, Some(s.value), noneW, noneD, noneH))
      case s: WeeklyAmount => Some((Salary.WEEKLY, noneY, noneM, Some(s.value), noneD, noneH))
      case s: DailyAmount => Some((Salary.DAILY, noneY, noneM, noneW, Some(s.value), noneH))
      case s: HourlyAmount => Some((Salary.HOURLY, noneY, noneM, noneW, noneD, Some(s.value)))
    }
  }

  def form(implicit messages: Messages) = Form(
    mapping(
      "salaryType" -> nonEmptyText,
      s"amount-$YEARLY" -> mandatoryIf(isEqual("salaryType", YEARLY), of(salaryValidation(YEARLY))),
      s"amount-$MONTHLY" -> mandatoryIf(isEqual("salaryType", MONTHLY), of(salaryValidation(MONTHLY))),
      s"amount-$WEEKLY" -> mandatoryIf(isEqual("salaryType", WEEKLY), of(salaryValidation(WEEKLY))),
      s"amount-$DAILY" -> mandatoryIf(isEqual("salaryType", DAILY), of(salaryValidation(DAILY))),
      s"amount-$HOURLY" -> mandatoryIf(isEqual("salaryType", HOURLY), of(salaryValidation(HOURLY)))
    )(formToSalary)(salaryToForm)
  )

  def formHourly(implicit messages: Messages) = Form(
    mapping(
      s"howManyHoursAWeek-$HOURLY" -> of(hoursValidation)
    )(Hours.apply)(Hours.unapply)
  )

//  def formDaily(implicit messages: Messages) = Form(
//    s"howManyDaysAWeek-$DAILY" -> of(dayValidation)
//  )

  def checkUserSelection(fieldElements: FieldElements, frequency: String): String = {
    if(fieldElements.field.value.contains(frequency)){
      "checked"
    } else {
      ""
    }
  }
}