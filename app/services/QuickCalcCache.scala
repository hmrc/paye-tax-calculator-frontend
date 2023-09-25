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
import models.{QuickCalcAggregateInput, QuickCalcMongoCache}
import respository.QuickCalcCacheMongo
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.mongo.cache.CacheIdType.SessionCacheId.NoSessionException
import utils.ServiceResponse

import scala.concurrent._

@ImplementedBy(classOf[QuickCalcKeyStoreCache])
trait QuickCalcCache {
  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]]

  def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): ServiceResponse[QuickCalcMongoCache]
}

@Singleton
class QuickCalcKeyStoreCache @Inject() (
  httpClient:                HttpClient,
  appConfig:                 AppConfig,
  quickCalcCacheMongo: QuickCalcCacheMongo
)(implicit executionContext: ExecutionContext)
    extends QuickCalcCache {

  val id = "quick-calc-aggregate-input"

  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
    quickCalcCacheMongo.findById(hc.sessionId.getOrElse(throw new  BadRequestException("No Session id found")).value).flatMap((test: Seq[QuickCalcMongoCache]) =>
      if(test.isEmpty){
        sessionCache.fetchAndGetEntry[QuickCalcAggregateInput](id)
      } else {
        Future.successful(test.headOption.map(_.quickCalcAggregateInput))
      })
  }
  def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): ServiceResponse[QuickCalcMongoCache] = {
    val cacheId = hc.sessionId.getOrElse(throw new BadRequestException("No Session id found"))
    quickCalcCacheMongo.add(QuickCalcMongoCache(cacheId.value,quickCalcAggregateInput = o))
  }

  private object sessionCache extends SessionCache {
    override lazy val http:          HttpClient = httpClient
    override lazy val defaultSource: String     = "paye-tax-calculator-frontend"
    override lazy val baseUri:       String     = appConfig.cacheUrl
    override lazy val domain:        String     = appConfig.domain
  }


}
