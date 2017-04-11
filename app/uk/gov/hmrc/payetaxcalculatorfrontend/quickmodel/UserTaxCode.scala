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

package uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel

import java.time.LocalDate

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.payeestimator.domain.{TaxCalcResource, TaxCalcResourceBuilder}
import uk.gov.hmrc.payeestimator.services.TaxCalculatorHelper
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.CustomFormatters._


case class UserTaxCode(gaveUsTaxCode: Boolean, taxCode: Option[String])

object UserTaxCode extends TaxCalculatorHelper {

  implicit val format = Json.format[UserTaxCode]

  val DEFAULT_SCOTTISH_TAC_CODE = "S1150L"
  val DEFAULT_TAX_CODE = "1150L"
  val HAS_TAX_CODE = "hasTaxCode"
  val TAX_CODE = "taxCode"

  private val startOfHardcodedTaxYear = LocalDate.of(2017, 4, 6)

  val suffixKeys = List('L', 'M', 'N', 'T')
  val WRONG_TAX_CODE_SUFFIX_KEY = "quick_calc.about_tax_code.wrong_tax_code_suffix"
  val WRONG_TAX_CODE_KEY = "quick_calc.about_tax_code.wrong_tax_code"
  val WRONG_TAX_CODE_NUMBER = "quick_calc.about_tax_code.wrong_tax_code_number"
  val WRONG_TAX_CODE_PREFIX_KEY = "quick_calc.about_tax_code.wrong_tax_code_prefix"
  val WRONG_TAX_CODE_EMPTY = "quick_calc.about_tax_code_empty_error"

  def taxCodeFormatter(implicit messages: Messages) = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      if (data.getOrElse(HAS_TAX_CODE, "false") == "true") {
        data.get(TAX_CODE).filter(_.nonEmpty)
          .map(_.toUpperCase()) match {
          case Some(taxCode) =>
            if (isValidTaxCode(taxCode, taxConfig(taxCode)))
              Right(Some(taxCode))
            else {
              Left(wrongTaxCode(taxCode))
            }
          case None => Left(Seq(FormError(TAX_CODE, messages(WRONG_TAX_CODE_EMPTY))))
        }
      } else Right(Some(DEFAULT_TAX_CODE))
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def wrongTaxCode(taxCode: String)(implicit messages: Messages): Seq[FormError] = {
    if(!taxCode.replaceAll("[^\\d.]", "").matches("^[0-9]{1,4}"))
      Seq(FormError(TAX_CODE, messages(WRONG_TAX_CODE_NUMBER)))
    else if(taxCode.replaceAll("([0-9])+([A-Z]?)+", "").matches("[A-JL-RT-Z]{1,2}"))
      Seq(FormError(TAX_CODE, messages(WRONG_TAX_CODE_PREFIX_KEY)))
    else if(taxCode.replaceAll("^([A-Z]?)+([0-9]?)+","").matches("[A-KO-SU-Z]"))
      Seq(FormError(TAX_CODE, messages(WRONG_TAX_CODE_SUFFIX_KEY)))
    else
      Seq(FormError(TAX_CODE, messages(WRONG_TAX_CODE_KEY)))
  }

  def form(implicit messages: Messages) = Form(
    mapping(
      HAS_TAX_CODE -> of(requiredBooleanFormatter),
      TAX_CODE -> of(taxCodeFormatter)
    )(UserTaxCode.apply)(UserTaxCode.unapply)
  )

  def checkUserSelection(checkFor: Boolean, taxCodeFromServer: Form[UserTaxCode]): String = {
    def htmlMapper(bO: Option[Boolean]): String =
      bO.filter(identity)
        .map(_ => "checked")
        .getOrElse("")

    def whatWasSelected(taxCode: Form[UserTaxCode]): Option[Boolean] = {
      taxCode.value.map( formData => formData.gaveUsTaxCode)
    }
    // Body
    htmlMapper(
      whatWasSelected(taxCodeFromServer).map(_ == checkFor))
  }

  def hideTextField(taxCode: Form[UserTaxCode]): String = {
    if (taxCode("taxCode").hasErrors) ""
    else {
      taxCode.value match {
        case Some(v) => if (v.gaveUsTaxCode) "" else "hidden"
        case _ => "hidden"
      }
    }
  }

  def taxConfig(taxCode: String): TaxCalcResource = TaxCalcResourceBuilder.resourceForDate(
    startOfHardcodedTaxYear,
    isValidScottishTaxCode(taxCode)
  )
}