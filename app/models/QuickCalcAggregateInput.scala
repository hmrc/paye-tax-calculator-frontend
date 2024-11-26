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

import config.AppConfig
import forms.AdditionalQuestionItem.{pensionContributionsFormat, postGraduateLoanContributionFormat, scottishIncomeFormat, studentLoanContributionFormat, taxCodeFormat}
import forms.{AdditionalQuestionItem, YouHaveToldUs, YouHaveToldUsItem}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

case class QuickCalcAggregateInput(
  savedSalary:                        Option[Salary],
  savedPeriod:                        Option[PayPeriodDetail],
  savedIsOverStatePensionAge:         Option[StatePension],
  savedTaxCode:                       Option[UserTaxCode],
  savedScottishRate:                  Option[ScottishRate],
  savedPensionContributions:          Option[PensionContributions],
  savedStudentLoanContributions:      Option[StudentLoanContributions],
  savedPostGraduateLoanContributions: Option[PostgraduateLoanContributions]) {

  def allQuestionsAnswered()(implicit appConfig: AppConfig): Boolean =
    List(
      savedSalary,
      savedIsOverStatePensionAge
    ).forall(_.isDefined)

  def youHaveToldUsItems(
  )(implicit m: Messages,
    appConfig:  AppConfig
  ): List[YouHaveToldUsItem] =
    List(
      savedSalary.map { YouHaveToldUs(_) },
      savedPeriod.map { YouHaveToldUs(_) },
      savedIsOverStatePensionAge.map { YouHaveToldUs(_) }
    ).flatten

  def additionalQuestionItems(
  )(implicit m: Messages,
    appConfig:  AppConfig
  ): List[AdditionalQuestionItem] =
    List(
      AdditionalQuestionItem(savedTaxCode),
      AdditionalQuestionItem(savedScottishRate),
      AdditionalQuestionItem(savedPensionContributions),
      AdditionalQuestionItem(savedStudentLoanContributions),
      AdditionalQuestionItem(savedPostGraduateLoanContributions)
    )

  def isEmpty: Boolean =
    List(
      savedSalary,
      savedIsOverStatePensionAge,
      savedTaxCode
    ).forall(_.isEmpty)

}

object QuickCalcAggregateInput {
  def newInstance:     QuickCalcAggregateInput          = QuickCalcAggregateInput(None, None, None, None, None, None, None, None)
  implicit val format: OFormat[QuickCalcAggregateInput] = Json.format[QuickCalcAggregateInput]
}
