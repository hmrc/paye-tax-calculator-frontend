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
import models.QuickCalcAggregateInput
import uk.gov.hmrc.calculator.CalculatorUtils

import java.time.{LocalDate, MonthDay}
import javax.inject.{Inject, Singleton}

@Singleton
class AggregateConditionsUtil @Inject() {

  def isTaxCodeDefined(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedTaxCode.flatMap(_.taxCode).isDefined

  def isPensionContributionsDefined(aggregateInput: QuickCalcAggregateInput) : Boolean =
    aggregateInput.savedPensionContributions.flatMap(_.monthlyContributionAmount).isDefined

  def givenPensionContributionPercentage(aggregateInput: QuickCalcAggregateInput) : Boolean =
    aggregateInput.savedPensionContributions.exists(_.gaveUsPercentageAmount)

  def taxCodeContainsS(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedTaxCode.flatMap(_.taxCode.map(_.contains("S"))).getOrElse(false)


  def payScottishRate(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedScottishRate.exists(_.payScottishRate)

}
