/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.MonthDay

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.payeestimator.domain.{TaxCalcResource, TaxCalcResourceBuilder}
import uk.gov.hmrc.payeestimator.services.TaxCalculatorHelper
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.CustomFormatters._
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.LocalDateProvider

case class UserTaxCode(gaveUsTaxCode: Boolean, taxCode: Option[String])

object UserTaxCode extends TaxCalculatorHelper {

  implicit val format: OFormat[UserTaxCode] = Json.format[UserTaxCode]

  def defaultScottishTaxCode: String = {
    if (currentTaxYear == 2018) Default2018ScottishTaxCode else Default2017ScottishTaxCode
  }

  private lazy val Default2017ScottishTaxCode = "S1150L"
  private lazy val Default2018ScottishTaxCode = "S1185L"

  def defaultUkTaxCode: String = {
    if (currentTaxYear == 2018) Default2018UkTaxCode else Default2017UkTaxCode
  }

  private lazy val Default2018UkTaxCode = "1185L"
  private lazy val Default2017UkTaxCode = "1150L"

  val HasTaxCode = "hasTaxCode"
  val TaxCode = "taxCode"

  val suffixKeys = List('L', 'M', 'N', 'T')
  val WrongTaxCodeSuffixKey = "quick_calc.about_tax_code.wrong_tax_code_suffix"
  val WrongTaxCodeKey = "quick_calc.about_tax_code.wrong_tax_code"
  val WrongTaxCodeNumber = "quick_calc.about_tax_code.wrong_tax_code_number"
  val WrongTaxCodePrefixKey = "quick_calc.about_tax_code.wrong_tax_code_prefix"
  val WrongTaxCodeEmpty = "quick_calc.about_tax_code_empty_error"

  def taxCodeFormatter(implicit messages: Messages) = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      if (data.getOrElse(HasTaxCode, "false") == "true") {
        data.get(TaxCode).filter(_.nonEmpty)
          .map(_.toUpperCase()) match {
          case Some(taxCode) =>
            if (isValidTaxCode(taxCode, taxConfig(taxCode)))
              Right(Some(taxCode))
            else {
              Left(wrongTaxCode(taxCode))
            }
          case None => Left(Seq(FormError(TaxCode, messages(WrongTaxCodeEmpty))))
        }
      } else Right(Some(defaultUkTaxCode))
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def wrongTaxCode(taxCode: String)(implicit messages: Messages): Seq[FormError] = {
    if (!taxCode.replaceAll("[^\\d.]", "").matches("^[0-9]{1,4}"))
      Seq(FormError(TaxCode, messages(WrongTaxCodeNumber)))
    else if (taxCode.replaceAll("([0-9])+([A-Z]?)+", "").matches("[A-JL-RT-Z]{1,2}"))
      Seq(FormError(TaxCode, messages(WrongTaxCodePrefixKey)))
    else if (taxCode.replaceAll("^([A-Z]?)+([0-9]?)+", "").matches("[A-KO-SU-Z]"))
      Seq(FormError(TaxCode, messages(WrongTaxCodeSuffixKey)))
    else
      Seq(FormError(TaxCode, messages(WrongTaxCodeKey)))
  }

  def form(implicit messages: Messages) = Form(
    mapping(
      HasTaxCode -> of(requiredBooleanFormatter),
      TaxCode -> of(taxCodeFormatter)
    )(UserTaxCode.apply)(UserTaxCode.unapply)
  )

  def checkUserSelection(checkFor: Boolean, taxCodeFromServer: Form[UserTaxCode]): String = {
    def htmlMapper(bO: Option[Boolean]): String =
      bO.filter(identity)
        .map(_ => "checked")
        .getOrElse("")

    def whatWasSelected(taxCode: Form[UserTaxCode]): Option[Boolean] = {
      taxCode.value.map(formData => formData.gaveUsTaxCode)
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
    startOfCurrentTaxYear,
    isValidScottishTaxCode(taxCode)
  )

  private def currentTaxYear: Int = {
    val now = LocalDateProvider.now

    if (now.isBefore(firstDayOfTaxYear.atYear(now.getYear))) {
      now.getYear - 1
    } else {
      now.getYear
    }
  }

  private def startOfCurrentTaxYear = {
    firstDayOfTaxYear.atYear(currentTaxYear)
  }

  private lazy val firstDayOfTaxYear = MonthDay.of(4, 6)
}
