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

package utils

import config.AppConfig

import java.time.{LocalDate, MonthDay}
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.calculator.CalculatorUtils

@Singleton
class DefaultTaxCodeProvider @Inject()(appConfig: AppConfig) {

  private lazy val firstDayOfTaxYear = MonthDay.of(4, 6)

  def defaultScottishTaxCode: String = {
    if (currentTaxYear == 2022) "S" + CalculatorUtils.INSTANCE.defaultTaxCode(2022).getTaxCode else "S" + CalculatorUtils.INSTANCE.defaultTaxCode(2021).getTaxCode
  }

  def defaultUkTaxCode: String =
    if (currentTaxYear == 2022) CalculatorUtils.INSTANCE.defaultTaxCode(2022).getTaxCode else CalculatorUtils.INSTANCE.defaultTaxCode(2021).getTaxCode

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
