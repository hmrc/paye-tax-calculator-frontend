/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.data.Form
import play.api.libs.json._

trait SalaryPeriod {
  val value: String
}

object SalaryPeriod {

  val id = "period"

  implicit val writes: Writes[SalaryPeriod] = Writes {
    reason => Json.obj(id -> reason.value)
  }

  val submissionWrites: Writes[SalaryPeriod] = Writes {
    reason => JsString(reason.value)
  }

  implicit val reads: Reads[SalaryPeriod] = (__ \ id).read[String].map {
    case Yearly.value => Yearly
    case Monthly.value => Monthly
    case FourWeekly.value => FourWeekly
    case Weekly.value => Weekly
    case Daily.value => Daily
    case Hourly.value => Hourly
  }

}

object Yearly extends SalaryPeriod {
  override val value = "a year"
  implicit val writes: Writes[Yearly.type] = Writes { _ => Json.obj("period" -> value) }
  val submissionWrites: Writes[Yearly.type] = Writes { _ => JsString(value)}
}

object Monthly extends SalaryPeriod {
  override val value = "a month"
  implicit val writes: Writes[Monthly.type] = Writes { _ => Json.obj("period" -> value) }
  val submissionWrites: Writes[Monthly.type] = Writes { _ => JsString(value)}
}

object FourWeekly extends SalaryPeriod {
  override val value = "every 4 weeks"
  implicit val writes: Writes[FourWeekly.type] = Writes { _ => Json.obj("period" -> value) }
  val submissionWrites: Writes[FourWeekly.type] = Writes { _ => JsString(value)}
}

object Weekly extends SalaryPeriod {
  override val value = "a week"
  implicit val writes: Writes[Weekly.type] = Writes { _ => Json.obj("period" -> value) }
  val submissionWrites: Writes[Weekly.type] = Writes { _ => JsString(value)}
}

object Daily extends SalaryPeriod {
  override val value = "a day"
  implicit val writes: Writes[Daily.type] = Writes { _ => Json.obj("period" -> value) }
  val submissionWrites: Writes[Daily.type] = Writes { _ => JsString(value)}
}

object Hourly extends SalaryPeriod {
  override val value = "an hour"
  implicit val writes: Writes[Hourly.type] = Writes { _ => Json.obj("period" -> value) }
  val submissionWrites: Writes[Hourly.type] = Writes { _ => JsString(value)}
}