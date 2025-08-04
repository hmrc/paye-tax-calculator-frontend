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

import play.api.libs.json.{JsString, Json}
import setup.BaseSpec

class StudentLoanPlanSpec extends BaseSpec{

    "StudentLoanPlan.PlanOne" should {

      "serialize to the correct JSON" in {
        Json.toJson(PlanOne) mustBe Json.obj(StudentLoanPlan.id -> PlanOne.value)
      }

      "serialize to the correct JSON when using submissionWrites" in {
        Json.toJson(PlanOne)(PlanOne.submissionWrites) mustBe JsString(PlanOne.value)
      }

      "deserialize from the correct JSON" in {
        Json.obj(StudentLoanPlan.id -> PlanOne.value).as[StudentLoanPlan] mustBe PlanOne
      }
    }

   "StudentLoanPlan.PlanTwo" should {

    "serialize to the correct JSON" in {
      Json.toJson(PlanTwo) mustBe Json.obj(StudentLoanPlan.id -> PlanTwo.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(PlanTwo)(PlanTwo.submissionWrites) mustBe JsString(PlanTwo.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(StudentLoanPlan.id -> PlanTwo.value).as[StudentLoanPlan] mustBe PlanTwo
    }
   }

   "StudentLoanPlan.PlanFour" should {

    "serialize to the correct JSON" in {
      Json.toJson(PlanFour) mustBe Json.obj(StudentLoanPlan.id -> PlanFour.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(PlanFour)(PlanFour.submissionWrites) mustBe JsString(PlanFour.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(StudentLoanPlan.id -> PlanFour.value).as[StudentLoanPlan] mustBe PlanFour
    }
   }

   "StudentLoanPlan.NoneOfThese" should {

    "serialize to the correct JSON" in {
      Json.toJson(NoneOfThese) mustBe Json.obj(StudentLoanPlan.id -> NoneOfThese.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(NoneOfThese)(NoneOfThese.submissionWrites) mustBe JsString(NoneOfThese.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(StudentLoanPlan.id -> NoneOfThese.value).as[StudentLoanPlan] mustBe NoneOfThese
    }
   }

}
