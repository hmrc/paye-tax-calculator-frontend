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

package utils

import uk.gov.hmrc.time.TaxYear

import java.time.{LocalDate, ZoneId}

object GetCurrentTaxYear {

  def getCurrentTaxYear: String = {
    val currentDate = LocalDate.now(ZoneId.of("Europe/London"))
    val taxYear = TaxYear(currentDate.getYear)
    if (currentDate.isBefore(taxYear.starts)) {
      val previousTaxYear = taxYear.previous
      s"${previousTaxYear.startYear}/${taxYear.startYear.toString.takeRight(2)}"
    } else {
      s"${taxYear.startYear}/${(taxYear.startYear + 1).toString.takeRight(2)}"
    }
  }

  def getTaxYear: Int =
    TaxYear.current.currentYear

}
