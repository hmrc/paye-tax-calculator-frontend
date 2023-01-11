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

package services

import com.google.inject.{ImplementedBy, Singleton}
import javax.inject.Inject
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import config.AppConfig
import models.QuickCalcAggregateInput
import uk.gov.hmrc.http.HttpClient

import scala.concurrent._

@ImplementedBy(classOf[QuickCalcKeyStoreCache])
trait QuickCalcCache {
  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]]

  def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap]
}

@Singleton
class QuickCalcKeyStoreCache @Inject() (
  httpClient:                HttpClient,
  appConfig:                 AppConfig
)(implicit executionContext: ExecutionContext)
    extends QuickCalcCache {

  val id = "quick-calc-aggregate-input"

  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] =
    sessionCache.fetchAndGetEntry[QuickCalcAggregateInput](id)

  def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = sessionCache.cache(id, o)

  private object sessionCache extends SessionCache {
    override lazy val http:          HttpClient = httpClient
    override lazy val defaultSource: String     = "paye-tax-calculator-frontend"
    override lazy val baseUri:       String     = appConfig.cacheUrl
    override lazy val domain:        String     = appConfig.domain
  }

}
