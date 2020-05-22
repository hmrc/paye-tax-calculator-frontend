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

package models

import play.api.libs.json._

case class Salary(
  amount:       BigDecimal,
  period:       String,
  howManyAWeek: Option[Double])

object Salary {

  import play.api.libs.functional.syntax._

  def salaryInPence(value: BigDecimal): Int =
    value.toInt

  implicit lazy val reads: Reads[Salary] = (
    (__ \ "amount").read[BigDecimal] and
    (__ \ "period").read[String] and
    (__ \ "how-many-a-week").readNullable[Double]
  )(Salary(_, _, _))

  implicit lazy val writes: Writes[Salary] =
    (
      (__ \ "amount").write[BigDecimal] and
      (__ \ "period").write[String] and
      (__ \ "how-many-a-week").writeNullable[Double]
    )(a => (a.amount, a.period, a.howManyAWeek))

}
