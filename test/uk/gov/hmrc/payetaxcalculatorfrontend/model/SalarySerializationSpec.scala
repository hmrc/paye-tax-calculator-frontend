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

package uk.gov.hmrc.payetaxcalculatorfrontend.model

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.play.test.UnitSpec

class SalarySerializationSpec extends UnitSpec {

  "Salary marshalling" should {
    "work for all salary types" in {
      jsonOf(Yearly(1)) shouldBe yearlyJson
      jsonOf(Monthly(1)) shouldBe monthlyJson
      jsonOf(Weekly(1)) shouldBe weeklyJson
      jsonOf(Daily(1,2)) shouldBe dailyJson
      jsonOf(Hourly(1,2)) shouldBe hourlyJson
    }
  }

  "Salary unmarshalling" should {
    "work for all salary types" in {
      unmarshalAndVerifyType[Yearly](yearlyJson)
      unmarshalAndVerifyType[Monthly](monthlyJson)
      unmarshalAndVerifyType[Weekly](weeklyJson)
      unmarshalAndVerifyType[Daily](dailyJson)
      unmarshalAndVerifyType[Hourly](hourlyJson)
    }
  }

  def yearlyJson = s"""{"value":1,"type":"${ Salary.YEARLY }"}"""
  def monthlyJson = s"""{"value":1,"type":"${ Salary.MONTHLY }"}"""
  def weeklyJson = s"""{"value":1,"type":"${ Salary.WEEKLY }"}"""
  def dailyJson = s"""{"value":1,"howManyAWeek":2,"type":"${ Salary.DAILY }"}"""
  def hourlyJson = s"""{"value":1,"howManyAWeek":2,"type":"${ Salary.HOURLY }"}"""

  def jsonOf[T : Writes](t: T): String = Json.stringify(Json.toJson(t))

  def unmarshalAndVerifyType[T](s: String)(implicit m: Manifest[T]) = Json.parse(s).as[Salary] shouldBe a[T]

  def unmarshal(s: String) = Json.parse(s).as[Salary]

}
