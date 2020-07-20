/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.MonthDay

import play.api.data.{Form, FormError}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.calculator.model.ValidationError
import uk.gov.hmrc.calculator.utils.validation.TaxCodeValidator
import utils.LocalDateProvider

case class UserTaxCode(
                        gaveUsTaxCode: Boolean = false,
                        taxCode:       Option[String])


object UserTaxCode {

  implicit val format: OFormat[UserTaxCode] = Json.format[UserTaxCode]
  private lazy val Default2018ScottishTaxCode     = "S1185L"
  private lazy val Default20192020ScottishTaxCode = "S1250L"
  private lazy val Default2018UkTaxCode           = "1185L"
  private lazy val Default20192020UkTaxCode       = "1250L"
  private lazy val firstDayOfTaxYear              = MonthDay.of(4, 6)
  val HasTaxCode                                  = "hasTaxCode"
  val TaxCode                                     = "taxCode"
  val suffixKeys                                  = List('L', 'M', 'N', 'T')
  val WrongTaxCodeSuffixKey                       = "quick_calc.about_tax_code.wrong_tax_code_suffix"
  val WrongTaxCodeKey                             = "quick_calc.about_tax_code.wrong_tax_code"
  val WrongTaxCodeNumber                          = "quick_calc.about_tax_code.wrong_tax_code_number"
  val WrongTaxCodePrefixKey                       = "quick_calc.about_tax_code.wrong_tax_code_prefix"
  val WrongTaxCodeEmpty                           = "quick_calc.about_tax_code_empty_error"

  def defaultScottishTaxCode: String =
    if (currentTaxYear == 2019 || currentTaxYear == 2020) Default20192020ScottishTaxCode else Default2018ScottishTaxCode

  def currentTaxYear: Int = {
    val now = LocalDateProvider.now

    if (now.isBefore(firstDayOfTaxYear.atYear(now.getYear))) {
      now.getYear - 1
    } else {
      now.getYear
    }
  }

  def defaultUkTaxCode: String =
    if (currentTaxYear == 2019 || currentTaxYear == 2020) Default20192020UkTaxCode else Default2018UkTaxCode

  def wrongTaxCode(taxCode: String): Seq[FormError] = {
    val res = TaxCodeValidator.INSTANCE.isValidTaxCode(taxCode)

    (res.isValid, res.getErrorType) match {
      case (false, ValidationError.WrongTaxCodeNumber) => Seq(FormError(TaxCode, WrongTaxCodeNumber))
      case (false, ValidationError.WrongTaxCodePrefix) => Seq(FormError(TaxCode, WrongTaxCodePrefixKey))
      case (false, ValidationError.WrongTaxCodeSuffix) => Seq(FormError(TaxCode, WrongTaxCodeSuffixKey))
      case _                                           => Seq(FormError(TaxCode, WrongTaxCodeKey))
    }
  }

  def startOfCurrentTaxYear: Int =
    firstDayOfTaxYear.atYear(currentTaxYear).getYear
}
