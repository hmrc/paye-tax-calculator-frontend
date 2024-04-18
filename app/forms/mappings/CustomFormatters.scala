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

package forms.mappings

import models.PensionContributions.{gaveUsPensionPercentage, monthlyPensionContribution, yearlyContributionAmount}
import models.QuickCalcAggregateInput
import models.UserTaxCode._
import play.api.data.FormError
import play.api.data.format.Formatter
import uk.gov.hmrc.calculator.model.TaxYear
import uk.gov.hmrc.calculator.utils.validation.{HoursDaysValidator, TaxCodeValidator, WageValidator}
import utils.BigDecimalFormatter
import utils.GetCurrentTaxYear.getTaxYear
import utils.StripCharUtil.{stripAll, stripPercentage, stripPound}

import scala.util.{Failure, Success, Try}

object CustomFormatters {

  def scottishRateValidation: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _       => Left(Seq(FormError(key, "quick_calc.scottish_rate_error")))
      }

    override def unbind(
      key:   String,
      value: Boolean
    ) = Map(key -> value.toString)
  }

  def hasScottishRateBooleanFormatter: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "" => Right(false)
        case _  => Right(true)
      }

    override def unbind(
      key:   String,
      value: Boolean
    ) =
      Map(key -> value.toString)
  }

  def removeTaxCodeValidation: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _ =>
          Left(
            Seq(
              FormError(key, "quick_calc.remove_tax_code_error")
            )
          )
      }

    override def unbind(
      key:   String,
      value: Boolean
    ) = Map(key -> value.toString)
  }

  def removePensionContributionsValidation: Formatter[Boolean] = new Formatter[Boolean] {
    override def bind(
                       key:  String,
                       data: Map[String, String]
                     ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _ =>
          Left(
            Seq(
              FormError(key, "quick_calc.remove_pensions_contributions_error")
            )
          )
      }

    override def unbind(key: String, value: Boolean): Map[String, String] = ???
  }

  def statePensionAgeValidation: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _ =>
          Left(
            Seq(
              FormError(key, "quick_calc.over_state_pension_age_error")
            )
          )
      }

    override def unbind(
      key:   String,
      value: Boolean
    ) = Map(key -> value.toString)
  }

  def hasTaxCodeBooleanFormatter: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "" => Right(false)
        case _  => Right(true)
      }

    override def unbind(
      key:   String,
      value: Boolean
    ) =
      Map(key -> value.toString)
  }

  def requiredSalaryPeriodFormatter: Formatter[String] = new Formatter[String] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "" => Left(Seq(FormError(key, "quick_calc.salary.option_error")))
        case p  => Right(p)
      }

    override def unbind(
      key:   String,
      value: String
    ) = Map(key -> value)
  }

  def dayValidation: Formatter[BigDecimal] = new Formatter[BigDecimal] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], BigDecimal] =
      Right(data.getOrElse(key, "")).right.flatMap {
        case s if s.nonEmpty =>
          try {
            val days = BigDecimal(s).setScale(2).toDouble
            if (days < 1.0) {
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.number_of_days.invalid_hours"
                  )
                )
              )
            } else if (days > 7.0) {
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.number_of_days.invalid_hours"
                  )
                )
              )
            } else {
              Right(BigDecimalFormatter.stripZeros(BigDecimal(s).bigDecimal))
            }
          } catch {
            case _: Throwable =>
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.number_of_days.invalid_number"
                  )
                )
              )
          }
        case _ =>
          Left(
            Seq(
              FormError(
                key,
                "quick_calc.salary.question.error.empty_number_daily"
              )
            )
          )
      }

    override def unbind(
      key:   String,
      value: BigDecimal
    ): Map[String, String] =
      Map(key -> value.toString)
  }

  def hoursValidation: Formatter[BigDecimal] = new Formatter[BigDecimal] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], BigDecimal] =
      Right(data.getOrElse(key, "")).right.flatMap {
        case s if s.nonEmpty =>
          try {
            val hours = BigDecimal(s).setScale(2).toDouble
            if (!HoursDaysValidator.INSTANCE
                  .isAboveMinimumHoursPerWeek(hours)) {
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.number_of_hours.invalid_number"
                  )
                )
              )
            } else if (!HoursDaysValidator.INSTANCE
                         .isBelowMaximumHoursPerWeek(hours)) {
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.number_of_hours.invalid_number"
                  )
                )
              )
            } else {
              Right(BigDecimalFormatter.stripZeros(BigDecimal(s).bigDecimal))
            }
          } catch {
            case _: Throwable =>
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.invalid_number_hourly"
                  )
                )
              )
          }
        case _ =>
          Left(
            Seq(
              FormError(
                key,
                "quick_calc.salary.question.error.empty_number_hourly"
              )
            )
          )
      }

    override def unbind(
      key:   String,
      value: BigDecimal
    ): Map[String, String] =
      Map(key -> value.toString)

  }

  def salaryValidation: Formatter[BigDecimal] = new Formatter[BigDecimal] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], BigDecimal] =
      (data.get(key).filter(_.nonEmpty) match {
        case Some(s) =>
          val strippedValue = stripAll(s)
          try {
            val salary = BigDecimal(strippedValue).setScale(2)
            if (!WageValidator.INSTANCE.isAboveMinimumWages(salary.toDouble)) {
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.minimum_salary_input"
                  )
                )
              )
            } else if (!WageValidator.INSTANCE
                         .isBelowMaximumWages(salary.toDouble)) {
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.maximum_salary_input"
                  )
                )
              )
            } else {
              Right(salary)
            }
          } catch {
            case _: Throwable if !strippedValue.matches("([0-9])+(\\.\\d+)") =>
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question_error_invalid_input"
                  )
                )
              )
            case _: Throwable if !strippedValue.matches("([0-9])+(\\.\\d{1,2})") =>
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.invalid_salary"
                  )
                )
              )
            case _: Throwable =>
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.invalid_salary"
                  )
                )
              )
          }
        case None =>
          Left(
            Seq(
              FormError(
                key,
                "quick_calc.salary.question.error.empty_salary_input"
              )
            )
          )
      })

    override def unbind(
      key:   String,
      value: BigDecimal
    ): Map[String, String] =
      Map(key -> value.toString)
  }

  def taxCodeFormatter: Formatter[Option[String]] =
    new Formatter[Option[String]] {

      override def bind(
        key:  String,
        data: Map[String, String]
      ): Either[Seq[FormError], Option[String]] =
        data
          .get(TaxCode)
          .filter(_.nonEmpty)
          .map(_.toUpperCase()) match {
          case Some(taxCode) =>
            if (TaxCodeValidator.INSTANCE.isValidTaxCode(taxCode).isValid)
              Right(Some(taxCode.replaceAll("\\s", "")))
            else {
              Left(wrongTaxCode(taxCode))
            }
          case None => Right(None)
        }

      override def unbind(
        key:   String,
        value: Option[String]
      ): Map[String, String] =
        Map(key -> value.getOrElse(""))
    }

  def pensionContributionFormatter(): Formatter[Option[BigDecimal]] =
    new Formatter[Option[BigDecimal]] {
      override def bind(
                         key: String,
                         data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] = {
        val gaveUsPensionPercentageData: String = data.getOrElse(gaveUsPensionPercentage, "")
        val gaveUsPercentage = Try(gaveUsPensionPercentageData.toBoolean).getOrElse(false)
        data.get(monthlyPensionContribution)
          .filter(_.nonEmpty)
          .map(_.replaceAll("/%", "")) match {
          case Some(p) =>
            val strippedValue = if (gaveUsPercentage) {
              stripPercentage(p)
            } else {
              stripPound(p)
            }
            val value = Try(BigDecimal(strippedValue))
            value match {
              case Success(amount) =>
                if (gaveUsPercentage) {
                  if (amount < 0) {
                    Left(
                      Seq(
                        FormError(
                          key,
                          "Enter your monthly pension contributions amount in the correct format"
                        )
                      )
                    )
                  } else if (amount > 100) {
                    Left(
                      Seq(
                        FormError(
                          key,
                          "Your monthly pension contributions must be less than 100% of your income"
                        )
                      )
                    )
                  } else if (strippedValue.split("\\.").lastOption.getOrElse("").length > 2) {
                  Left(
                    Seq(
                      FormError(
                        key,
                        "Enter your monthly pension contributions amount in the correct format"
                      )
                    )
                  )
                } else {
                    Right(Some(amount))
                  }
                } else {
                  if (amount < 0) {
                    Left(
                      Seq(
                        FormError(
                          key,
                          "Enter your monthly pension contributions amount in the correct format"
                        )
                      )
                    )
                  } else if (amount > 100) {
                    Right(Some(amount)) // Allow values more than £100
                  } else if (strippedValue.split("\\.").lastOption.getOrElse("").length > 2) {
                    Left(
                      Seq(
                        FormError(
                          key,
                          "Your monthly pension contributions can only include pounds and pence"
                        )
                      )
                    )
                  } else {
                    Right(Some(amount))
                  }
                }
              case Failure(_) =>
                Left(
                  Seq(
                    FormError(
                      key,
                      "Enter your monthly pension contributions amount in the correct format"
                    )
                  )
                )
            }
          case None => Right(None)
        }
      }

      override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
        value.map(v => Map(key -> v.toString)).getOrElse(Map.empty)
    }


}
