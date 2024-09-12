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

import play.api.libs.json._

trait StudentLoanPlan {
  val value: String
}

object StudentLoanPlan {

  val id = "studentLoanPlan"

  implicit val writes: Writes[StudentLoanPlan] = Writes { plan =>
    Json.obj(id -> plan.value)
  }

  val submissionWrites: Writes[StudentLoanPlan] = Writes { plan =>
    JsString(plan.value)
  }

  implicit val reads: Reads[StudentLoanPlan] = (__ \ id).read[String].map {
    case PlanOne.value     => PlanOne
    case PlanTwo.value     => PlanTwo
    case PlanFour.value    => PlanFour
    case NoneOfThese.value => NoneOfThese
  }

}

object PlanOne extends StudentLoanPlan {
  override val value: String = "plan one"

  implicit val writes: Writes[PlanOne.type] = Writes { _ =>
    Json.obj("studentLoanPlan" -> value)
  }

  val submissionWrites: Writes[PlanOne.type] = Writes { _ =>
    JsString(value)
  }
}

object PlanTwo extends StudentLoanPlan {
  override val value: String = "plan two"

  implicit val writes: Writes[PlanTwo.type] = Writes { _ =>
    Json.obj("studentLoanPlan" -> value)
  }

  val submissionWrites: Writes[PlanTwo.type] = Writes { _ =>
    JsString(value)
  }
}

object PlanFour extends StudentLoanPlan {
  override val value: String = "plan four"

  implicit val writes: Writes[PlanFour.type] = Writes { _ =>
    Json.obj("studentLoanPlan" -> value)
  }

  val submissionWrites: Writes[PlanFour.type] = Writes { _ =>
    JsString(value)
  }
}

object NoneOfThese extends StudentLoanPlan {
  override val value: String = "none of these"

  implicit val writes: Writes[NoneOfThese.type] = Writes { _ =>
    Json.obj("studentLoanPlan" -> value)
  }

  val submissionWrites: Writes[NoneOfThese.type] = Writes { _ =>
    JsString(value)
  }
}
