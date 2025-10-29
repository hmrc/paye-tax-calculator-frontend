/*
 * Copyright 2024 HM Revenue & Customs
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

package forms

import play.api.data.FormError
import play.api.data.format.Formatter

object StudentLoanPlanForm {

  val studentLoanPlan = "studentLoanPlan"
  val planOne: String = "plan one"
  val planTwo: String = "plan two"
  val planFour: String = "plan four"
  val planFive: String = "plan five"
  val noneOfThese: String = "none of these"

  val studentLoanPlanFormatter: Formatter[Option[StudentLoanPlan]] = new Formatter[Option[StudentLoanPlan]] {

    override def bind(
      key: String,
      data: Map[String, String]
    ): Either[Seq[FormError], Option[StudentLoanPlan]] =
      data.get(key) match {
        case Some(`planOne`)     => Right(Some(PlanOne))
        case Some(`planTwo`)     => Right(Some(PlanTwo))
        case Some(`planFour`)    => Right(Some(PlanFour))
        case Some(`planFive`)    => Right(Some(PlanFive))
        case Some(`noneOfThese`) => Right(Some(NoneOfThese))
        case _                   => Right(None) // Return Right(None) for unrecognized input
      }

    override def unbind(
      key: String,
      value: Option[StudentLoanPlan]
    ): Map[String, String] = {
      val stringValue = value match {
        case Some(PlanOne)     => planOne
        case Some(PlanTwo)     => planTwo
        case Some(PlanFour)    => planFour
        case Some(PlanFive)    => planFive
        case Some(NoneOfThese) => noneOfThese
      }
      Map(key -> stringValue)
    }
  }
}
