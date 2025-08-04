package repository

import config.AppConfig
import forms.Daily
import models.{PayPeriodDetail, QuickCalcAggregateInput, QuickCalcMongoCache, Salary}
import org.apache.pekko.Done
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import respository.QuickCalcCacheMongo
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import org.mongodb.scala.model.Filters
import org.scalatest.matchers.must.Matchers.*

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

class QuickCalcCacheMongoSpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[QuickCalcMongoCache]
    with MockitoSugar
    with OptionValues
    with ScalaFutures {

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)

  private val quickCalcMongoCache = QuickCalcMongoCache(
    "id",
    Instant.ofEpochSecond(1),
    quickCalcAggregateInput = QuickCalcAggregateInput(Some(Salary(12.00, None, None, Daily, Some(12.00), None)),
                                                      Some(PayPeriodDetail(12.00, 5.00, "period", "url")),
                                                      None,
                                                      None,
                                                      None,
                                                      None,
                                                      None,
                                                      None
                                                     )
  )
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
  private val mockAppConfig = mock[AppConfig]

  protected override val repository: QuickCalcCacheMongo = QuickCalcCacheMongo(
    mongo     = mongoComponent,
    appConfig = mockAppConfig,
    clock     = stubClock
  )

  ".add" - {
    "must set the last updated time on the QuickCalcMongoCache answers and save them" in {

      val expectedResult = quickCalcMongoCache.copy(createdAt = instant)

      val setResult = repository.add(quickCalcMongoCache).futureValue
      val updatedRecord = find(Filters.equal("id", quickCalcMongoCache.id)).futureValue.headOption.value

      setResult mustEqual Done
      updatedRecord mustEqual expectedResult
    }
  }

  "findById" - {
    "when there is a record for this id" - {
      "must update the lastUpdated time and get the record" in {
        insert(quickCalcMongoCache).futureValue

        val result = repository.findById(quickCalcMongoCache.id).futureValue
        val expectedResult = quickCalcMongoCache.copy(createdAt = instant)

        result.value mustEqual expectedResult
      }

    }
  }

}
