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

package uk.gov.hmrc.payetaxcalculatorfrontend.config

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.time.TaxYear

sealed trait Region
case object Scotland extends Region
case object EnglandWalesNI extends Region

trait TaxRefData {
  protected val config = ConfigFactory.load("tax-reference-data")

  def basicRate(implicit region: Region, taxYear: TaxYear): BigDecimal = asPercentage {
    config.getInt(s"${ taxYear.currentYear }.rates.$region.basic")
  }

  def higherRate(implicit region: Region, taxYear: TaxYear): BigDecimal = asPercentage {
    config.getInt(s"${ taxYear.currentYear }.rates.$region.higher")
  }

  def additionalRate(implicit region: Region, taxYear: TaxYear): BigDecimal = asPercentage {
    config.getInt(s"${ taxYear.currentYear }.rates.$region.additional")
  }

  private def asPercentage(i: Int): BigDecimal = BigDecimal(i) / 100

  def defaultPersonalAllowance(implicit taxYear: TaxYear): Int =
    config.getInt(s"${taxYear.currentYear}.personalAllowance")

  def taperedAllowanceLimit(implicit taxYear: TaxYear): Int =
    config.getInt(s"${taxYear.currentYear}.taperedAllowanceLimit")

  def basicRateBand(implicit region: Region, taxYear: TaxYear): Int =
    config.getInt(s"${taxYear.currentYear}.bands.$region.basic")

  def higherRateBand(implicit region: Region, taxYear: TaxYear): Int =
    additionalRateBand - basicRateBand

  def additionalRateBand(implicit region: Region, taxYear: TaxYear): Int =
    config.getInt(s"${taxYear.currentYear}.bands.$region.additional")

}
