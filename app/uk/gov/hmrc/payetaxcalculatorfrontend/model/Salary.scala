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

import play.api.libs.json._

sealed trait Salary
case class Yearly(value: BigDecimal) extends Salary
case class Weekly(value: BigDecimal) extends Salary
case class Monthly(value: BigDecimal) extends Salary
case class Daily(value: BigDecimal, howManyAWeek: Int) extends Salary
case class Hourly(value: BigDecimal, howManyAWeek: Int) extends Salary

object Salary {
  val YEARLY = "yearly"
  val WEEKLY = "weekly"
  val MONTHLY = "monthly"
  val DAILY = "daily"
  val HOURLY = "hourly"

  def addTypeInfo(s: String): JsObject => JsObject = _ ++ Json.obj("type" -> s)

  implicit val yearlyWrites = Json.writes[Yearly].transform(addTypeInfo(YEARLY))
  implicit val monthlyWrites = Json.writes[Monthly].transform(addTypeInfo(MONTHLY))
  implicit val weeklyWrites = Json.writes[Weekly].transform(addTypeInfo(WEEKLY))
  implicit val dailyWrites = Json.writes[Daily].transform(addTypeInfo(DAILY))
  implicit val hourlyWrites = Json.writes[Hourly].transform(addTypeInfo(HOURLY))

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
}
