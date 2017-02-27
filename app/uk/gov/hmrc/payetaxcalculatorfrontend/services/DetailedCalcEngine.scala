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

package uk.gov.hmrc.payetaxcalculatorfrontend.services

import uk.gov.hmrc.payetaxcalculatorfrontend.config.{Region, TaxRefData}
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.BigDecimalUtils.{max, min}
import uk.gov.hmrc.time.TaxYear

trait DetailedCalcEngine extends TaxRefData {
  def basicRateElement(taxableIncome: BigDecimal)(implicit region: Region, taxYear: TaxYear): BigDecimal =
    min(taxableIncome, basicRateBand)

  def higherRateElement(taxableIncome: BigDecimal)(implicit region: Region, taxYear: TaxYear): BigDecimal = {
    val earningsInHigherBand = max(taxableIncome - basicRateBand, 0)
    min(earningsInHigherBand, higherRateBand)
  }

  def additionalRateElement(taxableIncome: BigDecimal)(implicit region: Region, taxYear: TaxYear): BigDecimal =
    max(taxableIncome - basicRateBand - higherRateBand, 0)

  def calculateTax(earnings: BigDecimal)(implicit region: Region, taxYear: TaxYear): BigDecimal = {
    val taxableIncome = TaxableIncome.calculate(taperedAllowanceLimit, defaultPersonalAllowance)(earnings)
    val bre = basicRateElement(taxableIncome)
    val hre = higherRateElement(taxableIncome)
    val are = additionalRateElement(taxableIncome)

    (bre * basicRate) + (hre * higherRate) + (are * additionalRate)
  }
}