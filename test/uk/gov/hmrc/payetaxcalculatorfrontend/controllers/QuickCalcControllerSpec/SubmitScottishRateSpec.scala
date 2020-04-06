/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcControllerSpec

import org.jsoup.Jsoup
import org.scalatest.Tag
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.{QuickCalcController, routes}
import uk.gov.hmrc.payetaxcalculatorfrontend.quickmodel.{QuickCalcAggregateInput, ScottishRate, UserTaxCode}
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.BaseSpec
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup.cacheCompleteYearly

import scala.concurrent.Future

class SubmitScottishRate2018Spec extends BaseSpec {

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure("dateOverride" -> "2018-04-06")
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure("metrics.enabled" -> false)
    .build()

  "Submitting the Scottish rate form 2018" should {

    "return 400 Bad Request if the user does not select whether they pay the Scottish rate or not" in new Test {
      val res = testController.submitScottishRateForm()(fakeRequest)
      status(res) shouldBe BAD_REQUEST

      val html     = Jsoup.parse(contentAsString(res))
      val errorDiv = html.select("div#scottish-rate-inline-error")
      errorDiv should not be 'isEmpty
    }

    "return 303 See Other and redirect to the Check Your Answers page if they submit valid form data" in new Test {
      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(*, *)
        .once()
        .returning(emptyCacheMap)

      val res = testController.submitScottishRateForm()(fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true"))
      status(res) shouldBe SEE_OTHER

      redirectLocation(res) shouldBe Some(routes.QuickCalcController.summary().url)
    }

    "set the user's tax code to the 2018-19 default UK tax code " +
    "if the user does not pay the Scottish rate" taggedAs Tag("2018") in new Test {

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
        savedScottishRate = Some(ScottishRate(false)),
        savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("1185L")))
      )

      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(expectedAggregate, *)
        .once()
        .returning(emptyCacheMap)

      val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "false")
      val res = testController.submitScottishRateForm()(req)
      status(res) shouldBe SEE_OTHER
    }

    "set the user's tax code to the 2018-19 default Scottish tax code " +
    "if the user pays the Scottish rate" taggedAs Tag("2018") in new Test {

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
        savedScottishRate = Some(ScottishRate(true)),
        savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1185L")))
      )

      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(expectedAggregate, *)
        .once()
        .returning(emptyCacheMap)

      val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true")
      val res = testController.submitScottishRateForm()(req)
      status(res) shouldBe SEE_OTHER
    }
  }

  // mock expectations are automatically reset after each test
  trait Test {
    (mockCache
      .fetchAndGetEntry()(_: HeaderCarrier))
      .expects(*)
      .anyNumberOfTimes()
      .returning(Future.successful(cacheCompleteYearly))

    lazy val mockCache: QuickCalcCache = mock[QuickCalcCache]

    lazy val testController =
      new QuickCalcController(app.injector.instanceOf[MessagesApi], mockCache, stubControllerComponents())
    lazy val fakeRequest = FakeRequest().withHeaders(HeaderNames.xSessionId -> "some-session-id")

    lazy val emptyCacheMap: Future[CacheMap] = Future.successful(CacheMap("", Map.empty))
  }
}

class SubmitScottishRate2019Spec extends BaseSpec {
  "Submitting the Scottish rate form 2019 " should {
    "set the user's tax code to the 2019-20 default UK tax code " +
    "if the user does not pay the Scottish rate" taggedAs Tag("2019") in new Test {

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
        savedScottishRate = Some(ScottishRate(false)),
        savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("1250L")))
      )

      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(expectedAggregate, *)
        .once()
        .returning(emptyCacheMap)

      val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "false")
      val res = testController.submitScottishRateForm()(req)
      status(res) shouldBe SEE_OTHER
    }

    "return 400 Bad Request if the user does not select whether they pay the Scottish rate or not" in new Test {
      val res = testController.submitScottishRateForm()(fakeRequest)
      status(res) shouldBe BAD_REQUEST

      val html     = Jsoup.parse(contentAsString(res))
      val errorDiv = html.select("div#scottish-rate-inline-error")
      errorDiv should not be 'isEmpty
    }

    "return 303 See Other and redirect to the Check Your Answers page if they submit valid form data" in new Test {
      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(*, *)
        .once()
        .returning(emptyCacheMap)

      val res = testController.submitScottishRateForm()(fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true"))
      status(res) shouldBe SEE_OTHER

      redirectLocation(res) shouldBe Some(routes.QuickCalcController.summary().url)
    }
  }
  "set the user's tax code to the 2019-20 default Scottish tax code " +
  "if the user pays the Scottish rate" taggedAs Tag("2019") in new Test {

    val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
      savedScottishRate = Some(ScottishRate(true)),
      savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1250L")))
    )

    (mockCache
      .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
      .expects(expectedAggregate, *)
      .once()
      .returning(emptyCacheMap)

    val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true")
    val res = testController.submitScottishRateForm()(req)
    status(res) shouldBe SEE_OTHER
  }

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure("dateOverride" -> "2019-04-06")
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure("metrics.enabled" -> false)
    .build()

  // mock expectations are automatically reset after each test
  trait Test {
    (mockCache
      .fetchAndGetEntry()(_: HeaderCarrier))
      .expects(*)
      .anyNumberOfTimes()
      .returning(Future.successful(cacheCompleteYearly))

    lazy val mockCache: QuickCalcCache = mock[QuickCalcCache]

    lazy val testController =
      new QuickCalcController(app.injector.instanceOf[MessagesApi], mockCache, stubControllerComponents())
    lazy val fakeRequest = FakeRequest().withHeaders(HeaderNames.xSessionId -> "some-session-id")

    lazy val emptyCacheMap: Future[CacheMap] = Future.successful(CacheMap("", Map.empty))
  }
}

class SubmitScottishRate2020Spec extends BaseSpec {
  "Submitting the Scottish rate form 2020 " should {
    "set the user's tax code to the 2020-21 default UK tax code " +
    "if the user does not pay the Scottish rate" taggedAs Tag("2020") in new Test {

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
        savedScottishRate = Some(ScottishRate(false)),
        savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("1250L")))
      )

      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(expectedAggregate, *)
        .once()
        .returning(emptyCacheMap)

      val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "false")
      val res = testController.submitScottishRateForm()(req)
      status(res) shouldBe SEE_OTHER
    }

    "return 400 Bad Request if the user does not select whether they pay the Scottish rate or not" in new Test {
      val res = testController.submitScottishRateForm()(fakeRequest)
      status(res) shouldBe BAD_REQUEST

      val html     = Jsoup.parse(contentAsString(res))
      val errorDiv = html.select("div#scottish-rate-inline-error")
      errorDiv should not be 'isEmpty
    }

    "return 303 See Other and redirect to the Check Your Answers page if they submit valid form data" in new Test {
      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(*, *)
        .once()
        .returning(emptyCacheMap)

      val res = testController.submitScottishRateForm()(fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true"))
      status(res) shouldBe SEE_OTHER

      redirectLocation(res) shouldBe Some(routes.QuickCalcController.summary().url)
    }
  }
  "set the user's tax code to the 2020-21 default Scottish tax code " +
  "if the user pays the Scottish rate" taggedAs Tag("2020") in new Test {

    val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
      savedScottishRate = Some(ScottishRate(true)),
      savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1250L")))
    )

    (mockCache
      .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
      .expects(expectedAggregate, *)
      .once()
      .returning(emptyCacheMap)

    val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true")
    val res = testController.submitScottishRateForm()(req)
    status(res) shouldBe SEE_OTHER
  }

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure("dateOverride" -> "2020-04-06")
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure("metrics.enabled" -> false)
    .build()

  // mock expectations are automatically reset after each test
  trait Test {
    (mockCache
      .fetchAndGetEntry()(_: HeaderCarrier))
      .expects(*)
      .anyNumberOfTimes()
      .returning(Future.successful(cacheCompleteYearly))

    lazy val mockCache: QuickCalcCache = mock[QuickCalcCache]

    lazy val testController =
      new QuickCalcController(app.injector.instanceOf[MessagesApi], mockCache, stubControllerComponents())
    lazy val fakeRequest = FakeRequest().withHeaders(HeaderNames.xSessionId -> "some-session-id")

    lazy val emptyCacheMap: Future[CacheMap] = Future.successful(CacheMap("", Map.empty))
  }
}

class SubmitScottishRate2020MayOnwradsSpec extends BaseSpec {
  "Submitting the Scottish rate form 2020 May 11th Onwards" should {
    "set the user's tax code to the 2020-21 default UK tax code " +
    "if the user does not pay the Scottish rate" taggedAs Tag("2020") in new Test {

      val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
        savedScottishRate = Some(ScottishRate(false)),
        savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("1250L")))
      )

      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(expectedAggregate, *)
        .once()
        .returning(emptyCacheMap)

      val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "false")
      val res = testController.submitScottishRateForm()(req)
      status(res) shouldBe SEE_OTHER
    }

    "return 400 Bad Request if the user does not select whether they pay the Scottish rate or not" in new Test {
      val res = testController.submitScottishRateForm()(fakeRequest)
      status(res) shouldBe BAD_REQUEST

      val html     = Jsoup.parse(contentAsString(res))
      val errorDiv = html.select("div#scottish-rate-inline-error")
      errorDiv should not be 'isEmpty
    }

    "return 303 See Other and redirect to the Check Your Answers page if they submit valid form data" in new Test {
      (mockCache
        .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
        .expects(*, *)
        .once()
        .returning(emptyCacheMap)

      val res = testController.submitScottishRateForm()(fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true"))
      status(res) shouldBe SEE_OTHER

      redirectLocation(res) shouldBe Some(routes.QuickCalcController.summary().url)
    }
  }
  "set the user's tax code to the 2020-21 default Scottish tax code " +
  "if the user pays the Scottish rate" taggedAs Tag("2020") in new Test {

    val expectedAggregate: QuickCalcAggregateInput = cacheCompleteYearly.get.copy(
      savedScottishRate = Some(ScottishRate(true)),
      savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some("S1250L")))
    )

    (mockCache
      .save(_: QuickCalcAggregateInput)(_: HeaderCarrier))
      .expects(expectedAggregate, *)
      .once()
      .returning(emptyCacheMap)

    val req = fakeRequest.withFormUrlEncodedBody("scottishRate" -> "true")
    val res = testController.submitScottishRateForm()(req)
    status(res) shouldBe SEE_OTHER
  }

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure("dateOverride" -> "2020-05-11")
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure("metrics.enabled" -> false)
    .build()

  // mock expectations are automatically reset after each test
  trait Test {
    (mockCache
      .fetchAndGetEntry()(_: HeaderCarrier))
      .expects(*)
      .anyNumberOfTimes()
      .returning(Future.successful(cacheCompleteYearly))

    lazy val mockCache: QuickCalcCache = mock[QuickCalcCache]

    lazy val testController =
      new QuickCalcController(app.injector.instanceOf[MessagesApi], mockCache, stubControllerComponents())
    lazy val fakeRequest = FakeRequest().withHeaders(HeaderNames.xSessionId -> "some-session-id")

    lazy val emptyCacheMap: Future[CacheMap] = Future.successful(CacheMap("", Map.empty))
  }
}
