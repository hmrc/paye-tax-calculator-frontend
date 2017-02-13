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

package uk.gov.hmrc.payetaxcalculatorfrontend.setup

import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{JsBoolean, JsString}
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.payetaxcalculatorfrontend.WSHttp
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.model.{OverStatePensionAge, QuickCalcAggregateInput, UserTaxCode, Yearly}
import uk.gov.hmrc.payetaxcalculatorfrontend.services.QuickCalcCache
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object QuickCalcCacheSetup {

  def cache(mockedResultOfFetching: Option[QuickCalcAggregateInput]) = new QuickCalcCache {
    def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
      mockedResultOfFetching match {
        case None => Future.successful(None)
        case _ => Future.successful(mockedResultOfFetching)
      }
    }

    def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = {
      Future.successful(CacheMap("test-empty", Map.empty))
    }
  }

  val cacheEmpty = cache(None)

  val cacheReturnTaxCode = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L")))
  )))

  val cacheReturnTaxCodeAndIsOverStatePension = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L"))),
    isOverStatePensionAge = Some(OverStatePensionAge(true)))))

  val cacheReturnTaxCodeIsOverStatePensionAndSalary = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L"))),
    isOverStatePensionAge = Some(OverStatePensionAge(true)),
    salary = Some(Yearly(20000))
  )))

  val cacheReturnNoTaxCodeButAnswerEverythingElse = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    None,
    isOverStatePensionAge = Some(OverStatePensionAge(true)),
    salary = Some(Yearly(20000))
  )))

  val cacheReturnNoAgeButAnswerEverythingElse = cache(Some(QuickCalcAggregateInput.newInstance.copy(
    taxCode = Some(UserTaxCode(false, Some("1150L"))),
    None,
    salary = Some(Yearly(20000))
  )))
}
