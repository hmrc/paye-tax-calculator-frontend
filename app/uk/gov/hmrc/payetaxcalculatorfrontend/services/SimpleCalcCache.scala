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

package uk.gov.hmrc.payetaxcalculatorfrontend.services

import com.google.inject.{ImplementedBy, Singleton}
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.payetaxcalculatorfrontend.WSHttp
import uk.gov.hmrc.payetaxcalculatorfrontend.simplemodel.SimpleCalcAggregateInput
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

@ImplementedBy(classOf[SimpleCalcKeyStoreCache])
trait SimpleCalcCache {
  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[SimpleCalcAggregateInput]]

  def save(o: SimpleCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap]
}

@Singleton
class SimpleCalcKeyStoreCache extends SimpleCalcCache {
  val id = "simple-calc-aggregate-input"

  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[SimpleCalcAggregateInput]] = {
    sessionCache.fetchAndGetEntry[SimpleCalcAggregateInput](id)
  }

  def save(o: SimpleCalcAggregateInput)(implicit hc: HeaderCarrier) = sessionCache.cache(id, o)

  object sessionCache extends SessionCache with AppName with ServicesConfig {
    override lazy val http = WSHttp
    override lazy val defaultSource = appName
    override lazy val baseUri = baseUrl("cachable.session-cache")
    override lazy val domain = getConfString("cachable.session-cache.domain",
      throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
  }

}



