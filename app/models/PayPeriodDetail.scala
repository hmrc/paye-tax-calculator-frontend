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

import play.api.libs.json.{Json, OFormat, Reads, Writes, __}

case class PayPeriodDetail(
                            amount:       Int,
                            howManyAWeek: Double,
                            period:       String,
                            urlForChange: String)

object PayPeriodDetail {
  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[PayPeriodDetail] = (
    (__ \ "amount").read[Int] and
      (__ \ "how-many-a-week").read[Double] and
        (__ \ "period").read[String] and
        (__ \ "urlForChange").read[String]

    )(PayPeriodDetail(_, _, _,_))

  implicit lazy val writes: Writes[PayPeriodDetail] =
    (
      (__ \ "amount").write[Int] and
        (__ \ "how-many-a-week").write[Double] and
          (__ \ "period").write[String] and
        (__ \ "urlForChange").write[String]
      )(a => (a.amount, a.howManyAWeek, a.period, a.urlForChange))
}
