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

import play.api.i18n.Messages
import play.api.libs.json.Json

case class QuickCalcAggregateInput(salary: Option[Salary],
                                   isOverStatePensionAge: Option[OverStatePensionAge],
                                   scottishRate: Option[ScottishRate]){

  def allQuestionsAnswered: Boolean = List(salary, isOverStatePensionAge, scottishRate).forall(_.isDefined)

  def youHaveToldUsItems(implicit m: Messages): List[YouHaveToldUsItem] = {
    List(
//      salary.map { YouHaveToldUs(_ ) },
//      isOverStatePensionAge.map { YouHaveToldUs(_) },
//      taxCode.map { YouHaveToldUs(_) }
    ).flatten
  }

}

object QuickCalcAggregateInput {
  def newInstance = QuickCalcAggregateInput(None, None, None)
  implicit val format = Json.format[QuickCalcAggregateInput]
}


