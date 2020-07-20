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

package services

import javax.inject.Inject
import models.{PayPeriodDetail, QuickCalcAggregateInput, Salary}
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class SalaryService @Inject() (
  implicit val executionContext: ExecutionContext,
  navigator:                     Navigator) {

  def updateSalaryAmount(
    cache:            QuickCalcCache,
    salaryAmount:     Salary,
    url:              String
  )(implicit request: Request[AnyContent]
  ): Future[QuickCalcAggregateInput] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    cache
      .fetchAndGetEntry()
      .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
      .map { oldAggregate =>
        val newAggregate = oldAggregate.copy(savedSalary = Some(salaryAmount))
        (newAggregate.savedSalary, newAggregate.savedPeriod) match {
          case (Some(salary), Some(detail)) =>
            if (salary.period == oldAggregate.savedSalary.map(_.period).getOrElse("")) {
              newAggregate.copy(
                savedSalary =
                  Some(Salary(salaryAmount.amount, salary.period, oldAggregate.savedSalary.get.howManyAWeek)),
                savedPeriod = Some(PayPeriodDetail(salaryAmount.amount, detail.howManyAWeek, detail.period, url))
              )
            } else newAggregate.copy(savedPeriod = None)
          case _ => newAggregate.copy(savedPeriod = None)
        }
      }
  }
}

