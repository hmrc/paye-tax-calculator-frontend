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

import controllers.routes
import javax.inject.Inject
import models.QuickCalcAggregateInput
import play.api.mvc.{AnyContent, Call, Request, Result}

class Navigator @Inject() () {

  def nextPageOrSummaryIfAllQuestionsAnswered(
    aggregate:        QuickCalcAggregateInput
  )(next:             Call
  )(implicit request: Request[_]
  ): Call =
    if (aggregate.allQuestionsAnswered) routes.YouHaveToldUsController.summary()
    else next

  def tryGetShowStatePension(agg: QuickCalcAggregateInput)(implicit request: Request[AnyContent]): Call =
    nextPageOrSummaryIfAllQuestionsAnswered(agg) {
      routes.StatePensionController.showStatePensionForm()
    }

  def redirectToNotYetDonePage(aggregate: QuickCalcAggregateInput): Call =
    if (aggregate.savedSalary.isEmpty)
      routes.SalaryController.showSalaryForm()
    else if (aggregate.savedIsOverStatePensionAge.isEmpty)
      routes.StatePensionController.showStatePensionForm()
    else if (aggregate.savedTaxCode.isEmpty)
      routes.TaxCodeController.showTaxCodeForm()
    else
      routes.SalaryController.showSalaryForm()
}
