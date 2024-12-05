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

import forms.{FourWeekly, TaxResult}
import forms.TaxResult.moneyFormatter
import models.QuickCalcAggregateInput
import uk.gov.hmrc.calculator.utils.validation.PensionValidator
import uk.gov.hmrc.calculator.utils.validation.PensionValidator.PensionError
import utils.GetCurrentTaxYear.getTaxYear

import scala.jdk.CollectionConverters._
import javax.inject.{Inject, Singleton}

@Singleton
class AggregateConditionsUtil @Inject() {

  def isTaxCodeDefined(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedTaxCode.flatMap(_.taxCode).isDefined

  def isPensionContributionsDefined(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedPensionContributions.flatMap(_.monthlyContributionAmount).isDefined

  def givenPensionContributionPercentage(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedPensionContributions.exists(_.gaveUsPercentageAmount)

  def givenStudentLoanContribution(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedStudentLoanContributions.isDefined

  def givenPostGradLoanContribution(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedPostGraduateLoanContributions.isDefined

  def roundedMonthlySalary(aggregateInput: QuickCalcAggregateInput): String = {

    val monthlySalary: BigDecimal = aggregateInput.savedSalary.flatMap(_.monthlyAmount).getOrElse(BigDecimal(0))
    moneyFormatter(monthlySalary.setScale(2, BigDecimal.RoundingMode.HALF_UP))
  }

  def isPensionWarning(aggregateInput: QuickCalcAggregateInput): Boolean = {
    val monthlyContributions: BigDecimal =
      aggregateInput.savedPensionContributions.flatMap(_.monthlyContributionAmount).getOrElse(BigDecimal(0))
    val monthlySalary: BigDecimal = aggregateInput.savedSalary.flatMap(_.monthlyAmount).getOrElse(BigDecimal(0))
    val listOfPensionError = PensionValidator.INSTANCE
      .isValidMonthlyPension(monthlyContributions.toDouble,
                             monthlySalary.toDouble,
                             TaxResult.extractTaxYear(getTaxYear))
      .asScala
      .toList
    listOfPensionError.contains(PensionError.ABOVE_WAGE)
  }

  def isFourWeekly(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedSalary.exists { salary =>
      salary.period match {
        case fourWeekly: FourWeekly.type => true
        case _ => false
      }
    }

  def isScottishRateDefined(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedScottishRate.isDefined

}
