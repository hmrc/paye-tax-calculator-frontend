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

import models.PensionContributions.{gaveUsPensionPercentage, monthlyPensionContribution}
import models.UserTaxCode._
import play.api.data.FormError
import play.api.data.format.Formatter
import uk.gov.hmrc.calculator.model.pension.PensionMethod
import uk.gov.hmrc.calculator.utils.validation.PensionValidator.PensionError
import uk.gov.hmrc.calculator.utils.validation.{HoursDaysValidator, PensionValidator, TaxCodeValidator, WageValidator}
import utils.BigDecimalFormatter
import utils.StripCharUtil.{stripAll, stripPercentage, stripPound}

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object CustomFormatters {

  def scottishRateValidation: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _       => Left(Seq(FormError(key, "quick_calc.scottish_rate_error")))
      }

    override def unbind(
      key:   String,
      value: Boolean
    ): Map[String, String] = Map(key -> value.toString)
  }

  def hasScottishRateBooleanFormatter: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
        case "" => Right(false)
        case _  => Right(true)
      }

    override def unbind(
      key:   String,
      value: Boolean
    ): Map[String, String] =
      Map(key -> value.toString)
  }

  def removeTaxCodeValidation(): Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
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
    ): Map[String, String] = Map(key -> value.toString)
  }

  def removePensionContributionsValidation(): Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _ =>
          Left(
            Seq(
              FormError(key, "quick_calc.remove_pensions_contributions_error")
            )
          )
      }

    override def unbind(
      key:   String,
      value: Boolean
    ): Map[String, String] = Map(key -> value.toString)
  }

  def removeStudentLoanContributions(): Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
                       key:  String,
                       data: Map[String, String]
                     ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _ =>
          Left(
            Seq(
              FormError(key, "quick_calc.remove_student_loans_contributions_error")
            )
          )
      }

    override def unbind(
                         key:   String,
                         value: Boolean
                       ): Map[String, String] = Map(key -> value.toString)
  }

  def statePensionAgeValidation: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
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
    ): Map[String, String] = Map(key -> value.toString)
  }

  def postGraduateLoanValidation: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
                       key:  String,
                       data: Map[String, String]
                     ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
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
                       ): Map[String, String] = Map(key -> value.toString)
  }

  def hasTaxCodeBooleanFormatter: Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
        case "" => Right(false)
        case _  => Right(true)
      }

    override def unbind(
      key:   String,
      value: Boolean
    ): Map[String, String] =
      Map(key -> value.toString)
  }

  def requiredSalaryPeriodFormatter: Formatter[String] = new Formatter[String] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], String] =
      Right(data.getOrElse(key, "")).flatMap {
        case "" => Left(Seq(FormError(key, "quick_calc.salary.option_error")))
        case p  => Right(p)
      }

    override def unbind(
      key:   String,
      value: String
    ): Map[String, String] = Map(key -> value)
  }

  def studentLoanContributionsFormatter: Formatter[String] = new Formatter[String] {
    override def bind(
                       key:  String,
                       data: Map[String, String]
                     ): Either[Seq[FormError], String] =
      Right(data.getOrElse(key, "")).flatMap {
        case "" => Left(Seq(FormError(key, "quick_calc.salary.option_error")))
        case p  => Right(p)
      }

    override def unbind(
                         key:   String,
                         value: String
                       ): Map[String, String] = Map(key -> value)
  }

  def dayValidation: Formatter[BigDecimal] = new Formatter[BigDecimal] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], BigDecimal] =
      Right(data.getOrElse(key, "")).flatMap {
        case s if s.nonEmpty =>
          try {
            val days = BigDecimal(s).setScale(2).toDouble
            if (!HoursDaysValidator.INSTANCE.isAboveMinimumDaysPerWeek(days)) {
              Left(
                Seq(
                  FormError(
                    key,
                    "quick_calc.salary.question.error.number_of_days.invalid_hours"
                  )
                )
              )
            } else if (!HoursDaysValidator.INSTANCE.isBelowMaximumDaysPerWeek(days)) {
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
      Right(data.getOrElse(key, "")).flatMap {
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
        key:  String,
        data: Map[String, String]
      ): Either[Seq[FormError], Option[BigDecimal]] = {

        val gaveUsPensionPercentageData: String = data.getOrElse(gaveUsPensionPercentage, "")
        val gaveUsPercentage = Try(gaveUsPensionPercentageData.toBoolean).getOrElse(false)
        val pensionMethod    = if (gaveUsPercentage) PensionMethod.PERCENTAGE else PensionMethod.MONTHLY_AMOUNT_IN_POUNDS
        data
          .get(monthlyPensionContribution)
          .filter(_.nonEmpty)
          .map(_.replaceAll("/%", "")) match {
          case Some(monthlyPension) =>
            val strippedValue = if (gaveUsPercentage) stripPercentage(monthlyPension) else stripPound(monthlyPension)
            Try(BigDecimal(strippedValue)) match {
              case Success(amount) =>
                val pensionError: List[PensionError] = PensionValidator.INSTANCE
                  .validateValidInputPensionInput(amount.toDouble, pensionMethod)
                  .asScala
                  .toList
                if (pensionError.nonEmpty) {
                  pensionError.head match {
                    case PensionError.BELOW_ZERO =>
                      Left(Seq(FormError(key, "quick_calc.pensionContributionError.invalidFormat")))
                    case PensionError.ABOVE_HUNDRED_PERCENT =>
                      Left(
                        Seq(FormError(key, "quick_calc.pensionContributionError.lessThanHundredPercent"))
                      )
                    case PensionError.INVALID_PERCENTAGE_DECIMAL =>
                      Left(Seq(FormError(key, "quick_calc.pensionContributionError.invalidFormat")))
                    case PensionError.INVALID_AMOUNT_DECIMAL =>
                      Left(Seq(FormError(key, "quick_calc.pensionContributionError.poundAndPence")))
                    case _ =>
                      Left(Seq(FormError(key, "quick_calc.pensionContributionError.invalidFormat")))
                  }
                } else {
                  Right(Some(amount))
                }
              case Failure(_) =>
                Left(Seq(FormError(key, "quick_calc.pensionContributionError.invalidFormat")))
            }
          case None => Right(None)
        }
      }

      override def unbind(
        key:   String,
        value: Option[BigDecimal]
      ): Map[String, String] =
        value.map(v => Map(key -> v.toString)).getOrElse(Map.empty)
    }

}
