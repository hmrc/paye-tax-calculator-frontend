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


case class UserTaxCode(hasTaxCode: Boolean, taxCode: Option[String])

object UserTaxCode extends TaxCalculatorHelper {

  implicit val format = Json.format[UserTaxCode]

  val defaultTaxCode = "1150L"
  val hasTaxCode = "hasTaxCode"
  val taxCode = "taxCode"

  def taxCodeFormatter(implicit messages: Messages) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      if (data.getOrElse(hasTaxCode, "false") == "true") {
        if (isStandardTaxCode(data.getOrElse(taxCode, ""))) Right(Some(data.getOrElse(taxCode, "")))
        else {
          data.getOrElse(taxCode, "") match {
            case code if code.isEmpty => Left(Seq(FormError(taxCode, Messages("quick_calc.about_tax_code.wrong_tax_code"))))
            case code if code.nonEmpty => {
              if (data.getOrElse(taxCode, "").charAt(0).isDigit) Left(Seq(FormError(taxCode, Messages("quick_calc.about_tax_code.wrong_tax_code_suffix"))))
              else Left(Seq(FormError(taxCode, Messages("quick_calc.about_tax_code.wrong_tax_code"))))
            }
          }
        }
      }
      else Right(Some(defaultTaxCode))
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def form(implicit messages: Messages) = Form(
    mapping(
      hasTaxCode -> of(requiredBooleanFormatter),
      taxCode -> of(taxCodeFormatter)
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

  def taxConfig(taxCode: String): TaxCalcResource = TaxCalcResourceBuilder.resourceForDate(
    LocalDate.now(),
    isValidScottishTaxCode(taxCode))

}