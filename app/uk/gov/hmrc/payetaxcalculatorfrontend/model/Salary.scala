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

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import uk.gov.voa.play.form.ConditionalMappings._


sealed trait Salary
case class Yearly(value: BigDecimal) extends Salary
case class Weekly(value: BigDecimal) extends Salary
case class Monthly(value: BigDecimal) extends Salary
case class Daily(value: BigDecimal, howManyDaysAWeek: Int) extends Salary
case class Hourly(value: BigDecimal, howManyHoursAWeek: Int) extends Salary

object Salary {
  val YEARLY = "yearly"
  val WEEKLY = "weekly"
  val MONTHLY = "monthly"
  val DAILY = "daily"
  val HOURLY = "hourly"

  def addTypeInfo(s: String): JsObject => JsObject = _ ++ Json.obj("type" -> s)

  val yearlyWrites = Json.writes[Yearly].transform(addTypeInfo(YEARLY))
  val monthlyWrites = Json.writes[Monthly].transform(addTypeInfo(MONTHLY))
  val weeklyWrites = Json.writes[Weekly].transform(addTypeInfo(WEEKLY))
  val dailyWrites = Json.writes[Daily].transform(addTypeInfo(DAILY))
  val hourlyWrites = Json.writes[Hourly].transform(addTypeInfo(HOURLY))

  implicit val salaryWrites = new Writes[Salary] {
    def writes(o: Salary): JsValue = o match {
      case s: Yearly => yearlyWrites.writes(s)
      case s: Monthly => monthlyWrites.writes(s)
      case s: Weekly => weeklyWrites.writes(s)
      case s: Daily => dailyWrites.writes(s)
      case s: Hourly => hourlyWrites.writes(s)
    }
  }

  implicit val reads = new Reads[Salary] {
    def reads(json: JsValue): JsResult[Salary] = {
      (json \ "type").asOpt[String].map {
        case YEARLY => Json.reads[Yearly].reads(json)
        case WEEKLY => Json.reads[Weekly].reads(json)
        case MONTHLY => Json.reads[Monthly].reads(json)
        case DAILY => Json.reads[Daily].reads(json)
        case HOURLY => Json.reads[Hourly].reads(json)
      }.getOrElse(JsError("unable to parse as salary, type not found"))
    }
  }

  def formToSalary(salaryType: String,
                   amountYearly: Option[BigDecimal],
                   amountMonthly: Option[BigDecimal],
                   amountWeekly: Option[BigDecimal],
                   amountDaily: Option[BigDecimal],
                   amountHourly: Option[BigDecimal],
                   howManyAWeekDaily: Option[Int],
                   howManyAWeekHourly: Option[Int]): Salary = {
    salaryType match {
      case Salary.YEARLY => Yearly(amountYearly.get)
      case Salary.MONTHLY => Monthly(amountMonthly.get)
      case Salary.WEEKLY => Weekly(amountWeekly.get)
      case Salary.DAILY => Daily(amountDaily.get, howManyAWeekDaily.get)
      case Salary.HOURLY => Hourly(amountHourly.get, howManyAWeekHourly.get)
    }
  }

  def salaryToForm(salary: Salary) = {
    val noneY: Option[BigDecimal] = None
    val noneM: Option[BigDecimal] = None
    val noneW: Option[BigDecimal] = None
    val noneD: Option[BigDecimal] = None
    val noneH: Option[BigDecimal] = None
    val countDaily: Option[Int] = None
    val countHourly: Option[Int] = None
    salary match {
      case s: Yearly => Some((Salary.YEARLY, Some(s.value), noneM, noneW, noneD, noneH, countDaily, countHourly))
      case s: Monthly => Some((Salary.MONTHLY, noneY, Some(s.value), noneW, noneD, noneH, countDaily, countHourly))
      case s: Weekly => Some((Salary.WEEKLY, noneY, noneM, Some(s.value), noneD, noneH, countDaily, countHourly))
      case s: Daily => Some((Salary.DAILY, noneY, noneM, noneW, Some(s.value), noneH, Some(s.howManyDaysAWeek), countHourly))
      case s: Hourly => Some((Salary.HOURLY, noneY, noneM, noneW, noneD, Some(s.value), countDaily, Some(s.howManyHoursAWeek)))
    }
  }

  val form = Form(
    mapping(
      "salaryType" -> nonEmptyText,
      s"amount-$YEARLY" -> mandatoryIf(isEqual("salaryType", YEARLY), bigDecimal),
      s"amount-$MONTHLY" -> mandatoryIf(isEqual("salaryType", MONTHLY), bigDecimal),
      s"amount-$WEEKLY" -> mandatoryIf(isEqual("salaryType", WEEKLY), bigDecimal),
      s"amount-$DAILY" -> mandatoryIf(isEqual("salaryType", DAILY), bigDecimal),
      s"amount-$HOURLY" -> mandatoryIf(isEqual("salaryType", HOURLY), bigDecimal),
      s"howManyDaysAWeek-$DAILY" -> mandatoryIf(isEqual("salaryType", DAILY), number),
      s"howManyHoursAWeek-$HOURLY" -> mandatoryIf(isEqual("salaryType", HOURLY), number)
    )(formToSalary)(salaryToForm)
  )

}
