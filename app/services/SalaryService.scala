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

package services

import forms.{Daily, Hourly, TaxResult}

import javax.inject.Inject
import models.{PayPeriodDetail, QuickCalcAggregateInput, Salary}
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession

import scala.concurrent.{ExecutionContext, Future}

class SalaryService @Inject() (implicit val executionContext: ExecutionContext) {

  def updateSalaryAmount(
    cache:            QuickCalcCache,
    salaryAmount:     Salary,
    url:              String
  )(implicit request: Request[AnyContent]
  ): Future[QuickCalcAggregateInput] = {
    implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)
    cache
      .fetchAndGetEntry()
      .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
      .map { oldAggregate =>
        val newAggregate = salaryAmount.period match {
          case Hourly | Daily =>
            oldAggregate.copy(savedSalary = Some(
              salaryAmount.copy(
                amountYearly         = oldAggregate.savedSalary.flatMap(_.amountYearly),
                previousAmountYearly = oldAggregate.savedSalary.flatMap(_.previousAmountYearly),
                monthlyAmount        = oldAggregate.savedSalary.flatMap(_.monthlyAmount)
              )
            )
            )
          case _ =>
            val currentYearlyAmount: BigDecimal = {
              TaxResult.convertWagesToYearly(salaryAmount.amount, salaryAmount.period)
            }
            val monthlyAmount: BigDecimal = {
              TaxResult.convertWagesToMonthly(currentYearlyAmount)
            }
            if (oldAggregate.savedSalary.isDefined) {
              oldAggregate.copy(savedSalary = Some(
                salaryAmount.copy(amountYearly         = Some(currentYearlyAmount),
                                  previousAmountYearly = oldAggregate.savedSalary.flatMap(_.amountYearly),
                                  monthlyAmount        = Some(monthlyAmount))
              )
              )
            } else {
              oldAggregate.copy(savedSalary =
                Some(salaryAmount.copy(amountYearly = Some(currentYearlyAmount), monthlyAmount = Some(monthlyAmount)))
              )
            }
        }
        (newAggregate.savedSalary, newAggregate.savedPeriod) match {
          case (Some(salary), Some(detail)) =>
            if (salary.period == oldAggregate.savedSalary.map(_.period).getOrElse("")) {
              newAggregate.copy(
                savedSalary = Some(
                  Salary(
                    salaryAmount.amount,
                    newAggregate.savedSalary.flatMap(_.amountYearly),
                    newAggregate.savedSalary.flatMap(_.previousAmountYearly),
                    salary.period,
                    oldAggregate.savedSalary.get.howManyAWeek,
                    newAggregate.savedSalary.flatMap(_.monthlyAmount)
                  )
                ),
                savedPeriod = Some(PayPeriodDetail(salaryAmount.amount, detail.howManyAWeek, detail.period, url))
              )
            } else newAggregate.copy(savedPeriod = None)
          case _ => newAggregate.copy(savedPeriod = None)
        }
      }
  }
}
