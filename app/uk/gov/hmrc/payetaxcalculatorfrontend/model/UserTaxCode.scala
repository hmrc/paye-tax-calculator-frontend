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

package uk.gov.hmrc.payetaxcalculatorfrontend.model

import java.time.LocalDate

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.payeestimator.domain.TaxCalcResourceBuilder
import uk.gov.hmrc.payeestimator.services.LiveTaxCalculatorService.isValidScottishTaxCode
import uk.gov.hmrc.payetaxcalculatorfrontend.model.CustomFormatters._
import uk.gov.hmrc.payeestimator.services.TaxCalculatorHelper


case class UserTaxCode(hasTaxCode: Boolean, taxCode: Option[String])

object UserTaxCode extends TaxCalculatorHelper {

  implicit val format = Json.format[UserTaxCode]

  val DEFAULT_TAX_CODE = "1150L"
  val HAS_TAX_CODE = "hasTaxCode"
  val TAX_CODE = "taxCode"

  def taxCodeFormatter(implicit messages: Messages) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      if (data.getOrElse(HAS_TAX_CODE, "false") == "true") {
        data.get(TAX_CODE)
          .filter(_.nonEmpty) match {
          case Some(taxCode) =>
            if (isValidTaxCode(taxCode, taxConfig(taxCode)))
              Right(Some(taxCode))
            else {
              if (taxCode.charAt(0).isDigit)
                Left(Seq(FormError(TAX_CODE, messages("quick_calc.about_tax_code.wrong_tax_code_suffix"))))
              else
                Left(Seq(FormError(TAX_CODE, Messages("quick_calc.about_tax_code.wrong_tax_code"))))
            }
          case None => Left(Seq(FormError(TAX_CODE, Messages("quick_calc.about_tax_code.wrong_tax_code"))))
        }
      } else Right(Some(DEFAULT_TAX_CODE))
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def form(implicit messages: Messages) = Form(
    mapping(
      HAS_TAX_CODE -> of(requiredBooleanFormatter),
      TAX_CODE -> of(taxCodeFormatter)
    )(UserTaxCode.apply)(UserTaxCode.unapply)
  )

  def checkUserSelection(selection: Boolean, taxCode: Form[UserTaxCode]): String = {
    if (taxCode.hasErrors && selection) "checked"
    else if (selection)
      taxCode.value match {
        case Some(code) => if (code.hasTaxCode) "checked" else ""
        case _ => ""
      }
    else
      taxCode.value match {
        case Some(code) => if (!code.hasTaxCode && !taxCode.hasErrors) "checked" else ""
        case _ => ""
      }
  }

  def hideTextField(taxCode: Form[UserTaxCode]): String = {
    if (taxCode("taxCode").hasErrors) ""
    else {
      taxCode.value match {
        case Some(v) => if (v.hasTaxCode) "" else "hidden"
        case _ => "hidden"
      }
    }
  }

  def taxConfig(taxCode: String) = TaxCalcResourceBuilder.resourceForDate(
    LocalDate.now(),
    isValidScottishTaxCode(taxCode))

}
