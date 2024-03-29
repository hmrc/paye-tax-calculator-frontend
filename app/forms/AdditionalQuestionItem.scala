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
import models.{ScottishRate, UserTaxCode}
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

  implicit def taxCodeFormat(implicit messages: Messages): AdditionalQuestion[UserTaxCode] = new AdditionalQuestion[UserTaxCode] {

    def toAdditionalQuestionItem(t: Option[UserTaxCode]): AdditionalQuestionItem = {
      val label = "about_tax_code"
      val idSuffix = "tax-code"
      val url = routes.TaxCodeController.showTaxCodeForm.url

      val labelText = t.flatMap(_.taxCode) match {
        case Some(taxCode) => s"$taxCode"
        case None => Messages("quick_calc.you_have_told_us.about_tax_code.default.not.provided")
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
        val label = SCOTTISH_RATE
        val idSuffix = SCOTTISH_RATE
        val url = routes.ScottishRateController.showScottishRateForm.url
        AdditionalQuestionItem(
          (scottish.map(_.gaveUsScottishRate), scottish.map(_.payScottishRate)) match {
            case (Some(true), Some(true)) => Messages("quick_calc.you_have_told_us.scottish_rate.yes")
            case (Some(true), Some(false)) => Messages("quick_calc.you_have_told_us.scottish_rate.no")
            case _ => "No"
          },
          label,
          url,
          idSuffix
        )
      }
    }
}



