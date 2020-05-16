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

package forms.mappings

import cats.syntax.either._
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.i18n.Messages
import forms.UserTaxCode._
import uk.gov.hmrc.calculator.utils.validation.{HoursDaysValidator, TaxCodeValidator, WageValidator}

object CustomFormatters {

  def requiredBooleanFormatter(implicit messages: Messages): Formatter[Boolean] = new Formatter[Boolean] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _       => Left(Seq(FormError(key, Messages("select_one"))))
      }

    override def unbind(
      key:   String,
      value: Boolean
    ) = Map(key -> value.toString)
  }

  def requiredSalaryPeriodFormatter(implicit messages: Messages): Formatter[String] = new Formatter[String] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ) =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "" => Left(Seq(FormError(key, Messages("quick_calc.salary.option_error"))))
        case p  => Right(p)
      }

    override def unbind(
      key:   String,
      value: String
    ) = Map(key -> value.toString)
  }

  def dayValidation(implicit messages: Messages): Formatter[Double] = new Formatter[Double] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Double] =
      Right(data.getOrElse(key, "")).right.flatMap {
        case s if s.nonEmpty =>
          try {
            val days = s.toDouble
            if (days < 1.0) {
              Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.number_of_days.less_than_zero"))))
            } else if (days > 7.0) {
              Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.number_of_days.more_than_seven"))))
            } else {
              Right(days)
            }
          } catch {
            case _: Throwable =>
              Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.invalid_number_daily"))))
          }
        case _ => Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.empty_number_daily"))))
      }

    override def unbind(
      key:   String,
      value: Double
    ): Map[String, String] = Map(key -> value.toString)
  }

  def hoursValidation(implicit messages: Messages): Formatter[Double] = new Formatter[Double] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Double] =
      Right(data.getOrElse(key, "")).right.flatMap {
        case s if s.nonEmpty =>
          try {
            val hours: Double = s.toDouble
            if (!HoursDaysValidator.INSTANCE.isAboveMinimumHoursPerWeek(hours)) {
              Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.number_of_hours.less_than_one"))))
            } else if (!HoursDaysValidator.INSTANCE.isBelowMaximumHoursPerWeek(hours)) {
              Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.number_of_hours.more_than_168"))))
            } else {
              Right(hours)
            }
          } catch {
            case _: Throwable =>
              Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.invalid_number_hourly"))))
          }
        case _ => Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.empty_number_hourly"))))
      }

    override def unbind(
      key:   String,
      value: Double
    ): Map[String, String] = Map(key -> value.toString)

  }

  def salaryValidation(implicit messages: Messages): Formatter[BigDecimal] = new Formatter[BigDecimal] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], BigDecimal] =
      (data.get(key).filter(_.nonEmpty) match {
        case Some(s) =>
          try {
            val salary = BigDecimal(s).setScale(2)
            if (!WageValidator.INSTANCE.isAboveMinimumWages(salary.toDouble)) {
              Left("quick_calc.salary.question.error.minimum_salary_input")
            } else if (!WageValidator.INSTANCE.isBelowMaximumWages(salary.toDouble)) {
              Left("quick_calc.salary.question.error.maximum_salary_input")
            } else {
              Right(salary)
            }
          } catch {
            case _: Throwable if !s.matches("([0-9])+(\\.\\d+)") =>
              Left("quick_calc.salary.question_error_invalid_input")
            case _: Throwable if !s.matches("([0-9])+(\\.\\d{1,2})") =>
              Left("quick_calc.salary.question.error.maximum_salary_input")
            case _: Throwable => Left("quick_calc.salary.question.error.invalid_salary")
          }
        case None => Left("quick_calc.salary.question.error.empty_salary_input")
      }).leftMap(translationKey => Seq(FormError(key, Messages(translationKey))))

    override def unbind(
      key:   String,
      value: BigDecimal
    ): Map[String, String] = Map(key -> value.toString)
  }

  def hourlySalaryValidation(
    key:               String,
    data:              Map[String, String]
  )(implicit messages: Messages
  ): Either[Seq[FormError], BigDecimal] =
    Right(data.getOrElse(key, "")).right.flatMap {
      case s if s.nonEmpty =>
        try {
          val salary = BigDecimal(s).setScale(2)
          if (!WageValidator.INSTANCE.isAboveMinimumWages(salary.toDouble)) {
            Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.minimum_hourly_salary_input"))))
          } else if (!WageValidator.INSTANCE.isBelowMaximumWages(salary.toDouble)) {
            Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.maximum_salary_input"))))
          } else {
            Right(salary)
          }
        } catch {
          case _: Throwable => Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.invalid_salary"))))
        }

      case _ => Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.empty_hourly_salary_input"))))
    }

  def dailySalaryValidation(
    key:               String,
    data:              Map[String, String]
  )(implicit messages: Messages
  ) =
    Right(data.getOrElse(key, "")).right.flatMap {
      case s if s.nonEmpty =>
        try {
          val salary = BigDecimal(s).setScale(2)
          if (!WageValidator.INSTANCE.isAboveMinimumWages(salary.toDouble)) {
            Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.minimum_daily_salary_input"))))
          } else if (!WageValidator.INSTANCE.isBelowMaximumWages(salary.toDouble)) {
            Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.maximum_salary_input"))))
          } else {
            Right(salary)
          }
        } catch {
          case _: Throwable => Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.invalid_salary"))))
        }

      case _ => Left(Seq(FormError(key, Messages("quick_calc.salary.question.error.empty_daily_salary_input"))))
    }

  def taxCodeFormatter(implicit messages: Messages): Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(
      key:  String,
      data: Map[String, String]
    ): Either[Seq[FormError], Option[String]] =
      if (data.getOrElse(HasTaxCode, "false") == "true") {
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
          case None => Left(Seq(FormError(TaxCode, messages(WrongTaxCodeEmpty))))
        }
      } else Right(Some(defaultUkTaxCode))

    override def unbind(
      key:   String,
      value: Option[String]
    ): Map[String, String] = Map(key -> value.getOrElse(""))
  }

}
