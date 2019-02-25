/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.utils

import java.time.LocalDate

import play.api.Play.current

// ideally a Clock would be injected where LocalDate is used, but that would require
// refitting the entire project to use DI
object LocalDateProvider {
  def now: LocalDate = current.configuration.getString("dateOverride") match {
    case Some(s) => LocalDate.parse(s)
    case None => LocalDate.now
  }
}
