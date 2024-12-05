/*
 * Copyright 2024 HM Revenue & Customs
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
import models.{QuickCalcAggregateInput, QuickCalcMongoCache}
import org.apache.pekko.Done
import respository.QuickCalcCacheMongo

import java.time.Instant
import scala.concurrent._

@ImplementedBy(classOf[QuickCalcKeyStoreCache])
trait QuickCalcCache {
  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]]

  def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[Done]
}

@Singleton
class QuickCalcKeyStoreCache @Inject() (
  quickCalcCacheMongo:       QuickCalcCacheMongo
)(implicit executionContext: ExecutionContext)
    extends QuickCalcCache {

  val id = "quick-calc-aggregate-input"

  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] =
    quickCalcCacheMongo
      .findById(hc.sessionId.getOrElse(throw new BadRequestException("No Session id found")).value)
      .flatMap((test: Option[QuickCalcMongoCache]) => Future.successful(test.map(_.quickCalcAggregateInput)))

  def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[Done] = {
    val cacheId = hc.sessionId.getOrElse(throw new BadRequestException("No Session id found"))
    quickCalcCacheMongo.add(QuickCalcMongoCache(cacheId.value, quickCalcAggregateInput = o, createdAt = Instant.now()))
  }
}
