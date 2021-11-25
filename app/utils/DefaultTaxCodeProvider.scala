/*
 * Copyright 2021 HM Revenue & Customs
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

import config.AppConfig

import java.time.{LocalDate, MonthDay}
import javax.inject.{Inject, Singleton}

@Singleton
class DefaultTaxCodeProvider @Inject()(appConfig: AppConfig) {

  private lazy val Default2021UkTaxCode           = "1257L"
  private lazy val Default20192020UkTaxCode       = "1250L"
  private lazy val Default2021ScottishTaxCode     = "S1257L"
  private lazy val Default20192020ScottishTaxCode = "S1250L"
  private lazy val firstDayOfTaxYear              = MonthDay.of(4, 6)

  def defaultScottishTaxCode: String = {
    if (currentTaxYear == 2021) Default2021ScottishTaxCode else Default20192020ScottishTaxCode
  }

  def defaultUkTaxCode: String =
    if (currentTaxYear == 2021) Default2021UkTaxCode else Default20192020UkTaxCode

  def startOfCurrentTaxYear: Int =
    firstDayOfTaxYear.atYear(currentTaxYear).getYear

  def currentTaxYear: Int = {
    if (now.isBefore(firstDayOfTaxYear.atYear(now.getYear))) {
      now.getYear - 1
    } else {
      now.getYear
    }
  }

  private def now: LocalDate =
    appConfig.dateOverride match {
      case Some(s) => LocalDate.parse(s)
      case None    => LocalDate.now
    }

}

object DefaultTaxCodeProvider {
  def apply: DefaultTaxCodeProvider = DefaultTaxCodeProvider.apply
}
