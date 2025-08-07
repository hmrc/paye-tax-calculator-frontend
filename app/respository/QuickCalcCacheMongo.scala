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

package respository

import config.AppConfig
import models.QuickCalcMongoCache
import org.apache.pekko.Done
import org.mongodb.scala.bson.conversions.Bson
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, ReplaceOptions, Updates}
import org.mongodb.scala.model.Indexes.ascending

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class QuickCalcCacheMongo @Inject() (
  mongo: MongoComponent,
  appConfig: AppConfig,
  clock: Clock
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[QuickCalcMongoCache](
      collectionName = "quickCalcCache",
      mongoComponent = mongo,
      domainFormat   = QuickCalcMongoCache.format,
      indexes = Seq(
        IndexModel(ascending("createdAt"),
                   IndexOptions()
                     .background(false)
                     .name("createdAt")
                     .expireAfter(appConfig.mongoTtl, TimeUnit.SECONDS)
                  ),
        IndexModel(ascending("id"),
                   IndexOptions()
                     .background(false)
                     .name("id")
                     .unique(true)
                  )
      )
    ) {

  private def byId(id: String): Bson = Filters.equal("id", id)

  def add(quickCalcMongoCache: QuickCalcMongoCache): Future[Done] = {

    val updatedQuickCalcMongoCache = quickCalcMongoCache.copy(createdAt = Instant.now(clock))
    collection
      .replaceOne(filter = byId(updatedQuickCalcMongoCache.id), replacement = updatedQuickCalcMongoCache, options = ReplaceOptions().upsert(true))
      .toFuture()
      .map(_ => Done)
  }

  def keepAlive(id: String): Future[Done] =
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.set("createdAt", Instant.now(clock))
      )
      .toFuture()
      .map(_ => Done)

  def findById(id: String): Future[Option[QuickCalcMongoCache]] =
    keepAlive(id).flatMap { _ =>
      collection
        .find(byId(id))
        .headOption()
    }

}
