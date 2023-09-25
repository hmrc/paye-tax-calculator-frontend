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

package respository

import config.AppConfig
import errors.MongoDBError
import models.{QuickCalcAggregateInput, QuickCalcMongoCache}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Filters.equal
import utils.ServiceResponse

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class QuickCalcCacheMongo @Inject()(
                                          mongo: MongoComponent,
                                          appConfig: AppConfig
                                        )(implicit executionContext: ExecutionContext) extends PlayMongoRepository[QuickCalcMongoCache](
  collectionName = "quickCalcCache",
  mongoComponent = mongo,
  domainFormat = QuickCalcMongoCache.format,
  indexes = Seq(
    IndexModel(ascending("createdAt"),
      IndexOptions()
        .background(false)
        .name("createdAt")
        .expireAfter(appConfig.mongoTtl, TimeUnit.SECONDS)),
    IndexModel(ascending("id"),
      IndexOptions()
        .background(false)
        .name("id")
        .unique(true))
  )
){

  def add(quickCalcMongoCache: QuickCalcMongoCache): ServiceResponse[QuickCalcMongoCache] =
    collection
      .insertOne(quickCalcMongoCache)
      .toFuture()
      .map(_=> Right(quickCalcMongoCache))
      .recover {
        case _ => Left(MongoDBError("Unexpected error while writing a document"))
      }

  def findById(id: String): Future[Seq[QuickCalcMongoCache]] = {
     collection.find(equal("id", id)).toFuture()
  }

}
