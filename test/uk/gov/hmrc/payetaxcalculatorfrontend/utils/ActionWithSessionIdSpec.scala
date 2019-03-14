/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.utils

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.HeaderNames._
import play.api.mvc._
import play.api.test.FakeRequest
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.ActionWithSessionId.SessionIdNotFoundException
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec

class ActionWithSessionIdSpec extends UnitSpec with OneAppPerSuite with ScalaFutures with Results {

  "ActionWithSessionId" should {
    "include session-id in the result's session based on a value in headers" in {
      val expectedSessionId = "foo"
      val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> expectedSessionId)

      val result = ActionWithSessionId( _ => Ok )(request).futureValue

      session(result).get(SessionKeys.sessionId).get shouldBe expectedSessionId
    }
    "not modify session-id if already present in session" in {
      val expectedSessionId = "foo"
      val request = FakeRequest().withSession(SessionKeys.sessionId -> expectedSessionId)

      val result = ActionWithSessionId( _ => Ok )(request).futureValue

      session(result).get(SessionKeys.sessionId).get shouldBe expectedSessionId
    }
    "fail if session-id was not found in headers (which should never happen if SessionIdFilter is enabled)" in {
      val requestWithoutSessionId = FakeRequest()

      intercept[SessionIdNotFoundException] {
        ActionWithSessionId( _ => Ok )(requestWithoutSessionId).futureValue
      }
    }
  }

  def session(result: Result): Session =
    Cookies
      .fromCookieHeader(result.header.headers.get(SET_COOKIE))
      .get(Session.COOKIE_NAME)
      .map { cookie =>
        Session.decodeFromCookie(Some(cookie))
      }.get

}
