/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.wordspec.AnyWordSpecLike
import play.api.mvc.Result
import play.api.test.Helpers._
import setup.BaseSpec
import setup.QuickCalcCacheSetup._

import scala.concurrent.Future

class RedirectSpec extends BaseSpec with AnyWordSpecLike {

  "Redirect to Salary Form" should {
    "return 303" in {
      val controller = new QuickCalcController(messagesApi, cacheEmpty, stubControllerComponents(), navigator)
      val result: Future[Result] = controller.redirectToSalaryForm().apply(request)
      val status: Int    = await(result.map(_.header.status))

      val actualRedirect   = redirectLocation(result).get
      val expectedRedirect = s"${baseURL}your-pay"

      status      shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }
  }

}
