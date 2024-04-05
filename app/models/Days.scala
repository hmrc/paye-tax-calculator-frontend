/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{Reads, Writes, __}

case class Days(
  amount:       BigDecimal,
  howManyAWeek: BigDecimal)

object Days {
  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[Days] = (
    (__ \ "amount").read[BigDecimal] and
    (__ \ "how-many-a-week").read[BigDecimal]
  )(Days(_, _))

  implicit lazy val writes: Writes[Days] =
    (
      (__ \ "amount").write[BigDecimal] and
      (__ \ "how-many-a-week").write[BigDecimal]
    )(a => (a.amount, a.howManyAWeek))
}
