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

class SalaryPeriodSpec extends BaseSpec{

  "SalaryPeriod.Yearly" should {

    "serialize to the correct JSON" in {
      Json.toJson(Yearly) mustBe Json.obj(SalaryPeriod.id -> Yearly.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(Yearly)(Yearly.submissionWrites) mustBe JsString(Yearly.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(SalaryPeriod.id -> Yearly.value).as[SalaryPeriod] mustBe Yearly
    }
  }

  "SalaryPeriod.Monthly" should {

    "serialize to the correct JSON" in {
      Json.toJson(Monthly) mustBe Json.obj(SalaryPeriod.id -> Monthly.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(Monthly)(Monthly.submissionWrites) mustBe JsString(Monthly.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(SalaryPeriod.id -> Monthly.value).as[SalaryPeriod] mustBe Monthly
    }
  }

  "SalaryPeriod.FourWeekly" should {

    "serialize to the correct JSON" in {
      Json.toJson(FourWeekly) mustBe Json.obj(SalaryPeriod.id -> FourWeekly.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(FourWeekly)(FourWeekly.submissionWrites) mustBe JsString(FourWeekly.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(SalaryPeriod.id -> FourWeekly.value).as[SalaryPeriod] mustBe FourWeekly
    }
  }

  "SalaryPeriod.Weekly" should {

    "serialize to the correct JSON" in {
      Json.toJson(Weekly) mustBe Json.obj(SalaryPeriod.id -> Weekly.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(Weekly)(Weekly.submissionWrites) mustBe JsString(Weekly.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(SalaryPeriod.id -> Weekly.value).as[SalaryPeriod] mustBe Weekly
    }
  }

  "SalaryPeriod.Daily" should {

    "serialize to the correct JSON" in {
      Json.toJson(Daily) mustBe Json.obj(SalaryPeriod.id -> Daily.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(Daily)(Daily.submissionWrites) mustBe JsString(Daily.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(SalaryPeriod.id -> Daily.value).as[SalaryPeriod] mustBe Daily
    }
  }

  "SalaryPeriod.Hourly" should {

    "serialize to the correct JSON" in {
      Json.toJson(Hourly) mustBe Json.obj(SalaryPeriod.id -> Hourly.value)
    }

    "serialize to the correct JSON when using submissionWrites" in {
      Json.toJson(Hourly)(Hourly.submissionWrites) mustBe JsString(Hourly.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(SalaryPeriod.id -> Hourly.value).as[SalaryPeriod] mustBe Hourly
    }
  }

}
