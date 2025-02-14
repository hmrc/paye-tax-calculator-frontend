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

package forms

import controllers.routes
import models._
import play.api.i18n.Messages

case class AdditionalQuestionItem(
  value:    String,
  label:    String,
  url:      String,
  idSuffix: String)

trait AdditionalQuestion[A] {
  def toAdditionalQuestionItem(t: Option[A]): AdditionalQuestionItem
}

object AdditionalQuestionItem {

  val SCOTTISH_RATE = "scottish_rate"

  def apply[A: AdditionalQuestion](a: Option[A]): AdditionalQuestionItem =
    implicitly[AdditionalQuestion[A]].toAdditionalQuestionItem(a)

  implicit def taxCodeFormat(implicit messages: Messages): AdditionalQuestion[UserTaxCode] =
    new AdditionalQuestion[UserTaxCode] {

      def toAdditionalQuestionItem(t: Option[UserTaxCode]): AdditionalQuestionItem = {
        val label    = "about_tax_code"
        val idSuffix = "tax-code"
        val url      = routes.TaxCodeController.showTaxCodeForm.url

        val labelText = t.flatMap(_.taxCode) match {
          case Some(taxCode) => s"$taxCode"
          case None          => Messages("quick_calc.you_have_told_us.about_tax_code.default.not.provided")
        }

        AdditionalQuestionItem(
          labelText,
          label,
          url,
          idSuffix
        )
      }
    }

  implicit def scottishIncomeFormat(implicit messages: Messages) =
    new AdditionalQuestion[ScottishRate] {

      def toAdditionalQuestionItem(scottish: Option[ScottishRate]): AdditionalQuestionItem = {
        val label    = SCOTTISH_RATE
        val idSuffix = SCOTTISH_RATE
        val url      = routes.ScottishRateController.showScottishRateForm.url
        AdditionalQuestionItem(
          scottish
            .map { scottishValue =>
              scottishValue.payScottishRate match {
                case Some(true)  => Messages("quick_calc.you_have_told_us.scottish_rate.yes")
                case Some(false) => Messages("quick_calc.you_have_told_us.scottish_rate.no")
                case _           => Messages("not_provided")
              }
            }
            .getOrElse(Messages("not_provided")),
          label,
          url,
          idSuffix
        )
      }
    }

  implicit def pensionContributionsFormat(implicit messages: Messages): AdditionalQuestion[PensionContributions] =
    new AdditionalQuestion[PensionContributions] {

      def toAdditionalQuestionItem(pensions: Option[PensionContributions]): AdditionalQuestionItem = {
        val label    = "about_pensions_contributions"
        val idSuffix = "pension-contributions"
        val url      = routes.PensionContributionsPercentageController.showPensionContributionForm.url
        val formattedContribution = pensions
          .flatMap(_.monthlyContributionAmount)
          .map { contribution =>
            if (contribution.isWhole) {
              contribution.setScale(0).toString()
            } else {
              contribution.setScale(2, BigDecimal.RoundingMode.HALF_UP).toString().stripSuffix(".")
            }
          }
          .getOrElse("None")

        val displayText = if (formattedContribution == "None") {
          Messages("not_provided")
        } else {
          if (pensions.exists(_.gaveUsPercentageAmount)) {
            s"$formattedContribution% ${Messages("quick_calc.salary.monthly.label")}"
          } else {
            s"£$formattedContribution ${Messages("quick_calc.salary.monthly.label")}"
          }
        }

        AdditionalQuestionItem(displayText, label, url, idSuffix)
      }
    }

  implicit def studentLoanContributionFormat(
    implicit messages: Messages
  ): AdditionalQuestion[StudentLoanContributions] =
    new AdditionalQuestion[StudentLoanContributions] {

      def toAdditionalQuestionItem(t: Option[StudentLoanContributions]): AdditionalQuestionItem = {
        val label    = "about_student_loan_contribution"
        val idSuffix = "student-loan-contributions"
        val url      = routes.StudentLoanContributionsController.showStudentLoansForm.url

        val labelText = t.map(_.studentLoanPlan) match {
          case Some(studentLoanPlan) =>
            studentLoanPlan match {
              case Some(PlanOne)     => Messages("quick_calc.salary.studentLoan.plan1.text")
              case Some(PlanTwo)     => Messages("quick_calc.salary.studentLoan.plan2.text")
              case Some(PlanFour)    => Messages("quick_calc.salary.studentLoan.plan4.text")
              case Some(NoneOfThese) => Messages("not_provided")
              case _                 => Messages("not_provided")
            }
          case None => Messages("not_provided")
        }
        AdditionalQuestionItem(
          labelText,
          label,
          url,
          idSuffix
        )
      }
    }

  implicit def postGraduateLoanContributionFormat(
    implicit messages: Messages
  ): AdditionalQuestion[PostgraduateLoanContributions] =
    new AdditionalQuestion[PostgraduateLoanContributions] {

      def toAdditionalQuestionItem(postGrad: Option[PostgraduateLoanContributions]): AdditionalQuestionItem = {
        val label    = "about_post_graduate_loan_contribution"
        val idSuffix = "post_graduate_loan_contribution"
        val url      = routes.PostgraduateController.showPostgraduateForm.url
        AdditionalQuestionItem(
          postGrad
            .map { postGradValue =>
              postGradValue.hasPostgraduatePlan match {
                case Some(true)  => Messages("yes")
                case Some(false) => Messages("no")
                case _           => Messages("not_provided")
              }
            }
            .getOrElse(Messages("not_provided")),
          label,
          url,
          idSuffix
        )
      }
    }
}
