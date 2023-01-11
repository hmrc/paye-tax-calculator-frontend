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

package controllers

trait CSRFTestHelper {

  // As we compare pages in a String format for our tests, it is necessary to remove the value of the hidden csrf token
  // as it uses a randomly generated UUID
  def removeCSRFTagValue(content: String): String = {
    val csrfValueIndex = content.indexOf("\"csrfToken\" value=\"") + 18
    val firstHalf = content.substring(0, csrfValueIndex)
    val secondHalf = content.substring(csrfValueIndex+80)
    firstHalf.concat(secondHalf)
  }

}
