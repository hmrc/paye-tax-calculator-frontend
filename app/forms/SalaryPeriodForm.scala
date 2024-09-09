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

import play.api.data.Forms.{of, single}
import play.api.data.{Form, FormError}
import play.api.data.format.Formatter

object SalaryPeriodForm {

  val period: String = "period"
  val yearly: String = "a year"
  val monthly: String = "a month"
  val fourWeekly: String = "every 4 weeks"
  val weekly: String = "a week"
  val daily: String = "a day"
  val hourly: String = "an hour"

  val error: String = "quick_calc.salary.option_error"

  val salaryPeriodFormatter: Formatter[SalaryPeriod] = new Formatter[SalaryPeriod] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], SalaryPeriod] = {
      data.get(key) match {
        case Some(`yearly`) => Right(Yearly)
        case Some(`monthly`) => Right(Monthly)
        case Some(`fourWeekly`) => Right(FourWeekly)
        case Some(`weekly`) => Right(Weekly)
        case Some(`daily`) => Right(Daily)
        case Some(`hourly`) => Right(Hourly)
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: SalaryPeriod): Map[String, String] = {
      val stringValue = value match {
        case Yearly => yearly
        case Monthly => monthly
        case FourWeekly => fourWeekly
        case Weekly => weekly
        case Daily => daily
        case Hourly => hourly
      }
      Map(key -> stringValue)
    }
  }

}
