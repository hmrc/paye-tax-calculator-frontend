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

package models

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{Json, Writes}

class SalarySerializationSpec extends AnyWordSpecLike with Matchers {

  "Salary marshalling" should {
    "work for all salary types" in {
      jsonOf(Salary(1, None, None, "yearly", None, None))  shouldBe yearlyJson
      jsonOf(Salary(1, None, None, "monthly", None, None)) shouldBe monthlyJson
      jsonOf(Salary(1, None, None, "weekly", None, None))  shouldBe weeklyJson
      jsonOf(Days(1, 2))                             shouldBe dailyJson
      jsonOf(Hours(1, 2))                            shouldBe hourlyJson
    }
  }

  "Salary unmarshalling" should {
    "work for all salary types" in {
      unmarshalAndVerifyType[Salary](yearlyJson)
      unmarshalAndVerifyType[Salary](monthlyJson)
      unmarshalAndVerifyType[Salary](weeklyJson)
      unmarshalAndVerifyTypeDay[Days](dailyJson)
      unmarshalAndVerifyTypeHour[Hours](hourlyJson)
    }
  def yearlyJson  = s"""{"amount":1,"period":"yearly"}"""
  def monthlyJson = s"""{"amount":1,"period":"monthly"}"""
  def weeklyJson  = s"""{"amount":1,"period":"weekly"}"""
  def dailyJson   = s"""{"amount":1,"how-many-a-week":2}"""
  def hourlyJson  = s"""{"amount":1,"how-many-a-week":2}"""

  def jsonOf[T: Writes](t: T): String = Json.stringify(Json.toJson(t))

  def unmarshalAndVerifyType[T](s:     String)(implicit m: Manifest[T]): Assertion = Json.parse(s).as[Salary] shouldBe a[T]
  def unmarshalAndVerifyTypeDay[T](s:  String)(implicit m: Manifest[T]): Assertion = Json.parse(s).as[Days]   shouldBe a[T]
  def unmarshalAndVerifyTypeHour[T](s: String)(implicit m: Manifest[T]): Assertion = Json.parse(s).as[Hours]  shouldBe a[T]

  def unmarshal(s: String): Salary = Json.parse(s).as[Salary]

}
