package uk.gov.hmrc.payetaxcalculatorfrontend.controller

import akka.stream.{ActorMaterializer, Materializer}
import org.jsoup.Jsoup
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.Play.current
import play.api.data.Form
import play.api.http.HttpEntity
import play.api.libs.crypto.{AESCTRCrypter, CryptoConfig}
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.mvc._
import play.api.test._
import play.filters.csrf._
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.QuickCalcController
import uk.gov.hmrc.payetaxcalculatorfrontend.model._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.payetaxcalculatorfrontend.model.QuickCalcAggregateInput
import uk.gov.hmrc.payetaxcalculatorfrontend.services.{QuickCalcCache, QuickCalcKeyStoreCache}
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.test.Helpers._
import uk.gov.hmrc.payetaxcalculatorfrontend.WSHttp

/**
  * Created by paul on 06/02/17.
  */
class QuickCalcControllerSpec() extends UnitSpec with Results with OneAppPerSuite {

  val appInjector = app.injector

  val csrfAddToken = appInjector.instanceOf[CSRFAddToken]

  implicit val materializer = appInjector.instanceOf[Materializer]

  implicit val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "test")

  implicit val messages = Messages(Lang.defaultLang, appInjector.instanceOf[MessagesApi])

  object SessionCacheTest extends SessionCache with ServicesConfig{
    override def defaultSource = "test"
    override def baseUri = baseUrl("cachable.session-cache-test")
    override def domain = getConfString("cachable.session-cache.domain-test",
      throw new Exception(s"Could not find config 'cachable.session-cache.domain-test'"))
    override def http = WSHttp
  }

  implicit object CacheEmptyTest extends QuickCalcCache {
    val testId = "test-Id"

    override def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
      Future[Option[QuickCalcAggregateInput]](None)
    }
    override def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = {
      Future[CacheMap](
        SessionCacheTest.cache(testId, o)
      )
    }
  }

  implicit object CacheTaxCodeTest extends QuickCalcCache {
    val testId = "test-Id"

    override def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
      Future[Option[QuickCalcAggregateInput]](Some(QuickCalcAggregateInput(Some(UserTaxCode(true,Some("1150L"))),None,None)))
    }
    override def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = {
      Future[CacheMap](new CacheMap(testId, Map("hasTaxCode" -> JsString(o.taxCode.get.hasTaxCode.toString), "code" -> JsString(o.taxCode.get.taxCode.get))))
    }
  }

  implicit object CacheAgeTest extends QuickCalcCache {
    val testId = "test-id"
    override def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
      Future[Option[QuickCalcAggregateInput]](Some(QuickCalcAggregateInput(Some(UserTaxCode(true,Some("1150L"))),Some(OverStatePensionAge(true)),None)))
    }
    override def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = {
      Future[CacheMap](new CacheMap("test", Map("test" -> JsString("test-cache"))))
    }
  }

  implicit object CacheAllTest extends QuickCalcCache {
    val testId = "test-id"

    override def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
      Future[Option[QuickCalcAggregateInput]](Some(QuickCalcAggregateInput(Some(UserTaxCode(true,Some("1150L"))),Some(OverStatePensionAge(true)),Option(Yearly(20000)))))
    }
    override def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = {
      Future[CacheMap](new CacheMap("test", Map("test" -> JsString("test-cache"))))
    }
  }

  implicit object CacheFinalNoTaxCodeTest extends QuickCalcCache {
    val testId = "test-id"

    override def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[QuickCalcAggregateInput]] = {
      Future[Option[QuickCalcAggregateInput]](Some(QuickCalcAggregateInput(None,Some(OverStatePensionAge(true)),Option(Yearly(20000)))))
    }
    override def save(o: QuickCalcAggregateInput)(implicit hc: HeaderCarrier): Future[CacheMap] = {
      Future[CacheMap](new CacheMap("test", Map("test" -> JsString("test-cache"))))
    }
  }


  "Redirect to the Tax Code Form" should {
    "return 303" in {
      val controller = new QuickCalcController(messages.messages, CacheEmptyTest)
      val result = controller.redirectToTaxCodeForm().apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      status shouldBe 303
    }
  }

  "Show the Tax Code Form" should {
    "return 200 and an empty list of aggregate data if no aggregate data is present" in {
      val controller = new QuickCalcController(messages.messages, CacheEmptyTest)
      val action = csrfAddToken(controller.showTaxCodeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      status shouldBe 200
      parseHtml.getElementsByTag("tr").size shouldBe 0
    }

    "return 200 and a list of current aggregate data containing Tax Code: 1150L" in {
      val controller = new QuickCalcController(messages.messages, CacheTaxCodeTest)
      val action = csrfAddToken(controller.showTaxCodeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val items = List(YouHaveToldUsItem("1150L", "Tax Code", "/foo"))
      val expectedTaxCode = "1150L"

      val expectedNumberOfRows = 1 + items.size //Including header
      val actualTaxCode = parseHtml.getElementsByTag("tr").get(1).getElementsByTag("span").get(0).text()

      status shouldBe 200
      parseHtml.getElementsByTag("tr").size shouldBe expectedNumberOfRows
      actualTaxCode shouldBe expectedTaxCode
    }
  }

  "Submit Tax Code Form" should {
    "return 400, current valid aggregate data and an error message if Tax Code provided is invalid" in {
      val controller = new QuickCalcController(messages.messages, CacheTaxCodeTest)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true,Some("110")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedErrorMessage = "Please check and re-enter your tax code"
      val list = List(YouHaveToldUsItem("1150L", "Tax Code", "/foo"))
      val expectedTableSize = 1 + list.size // include header

      val actualTableSize = parseHtml.getElementsByTag("tr").size()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedErrorMessage
      actualTableSize shouldBe expectedTableSize
    }

    "return 400, empty list of aggregate data and an error message if Tax Code provided is invalid" in {
      val controller = new QuickCalcController(messages.messages, CacheEmptyTest)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true,Some("110")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test"))
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedErrorMessage = "Please check and re-enter your tax code"

      val actualTableSize = parseHtml.getElementsByTag("tr").size()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400
      actualErrorMessage shouldBe expectedErrorMessage
      actualTableSize shouldBe 0
    }

    "return 303, with the new Tax Code saved on the current list of aggregate data without Age and Salary" in {
      val controller = new QuickCalcController(messages.messages, CacheTaxCodeTest)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true,Some("K425")))
      val postAction = await(csrfAddToken(controller.submitTaxCodeForm()))
      val postResult = postAction(request.withFormUrlEncodedBody(formTax.data.toSeq:_*)).withSession(request.session + (SessionKeys.sessionId -> "test"))
      val status = postResult.header.status

      val getAction = csrfAddToken(controller.showAgeForm())
      val getResult = getAction(request).withSession(request.session + (SessionKeys.sessionId -> "test"))

      val responseBody = contentAsString(getResult)
      val parseHtml = Jsoup.parse(responseBody)

      status shouldBe 303

    }

    "return 303, with the new Tax Code added to a new list of aggregate data" in {

    }

    "return 303, with new Tax Code saved on the current list of aggregate data which contains all answered questions" in {

    }

  }

  "Show Age Form" should {

    "return 200, with current list of aggregate data containing Tax Code: 1150L and \"YES\" for is not Over65" in {
      val controller = new QuickCalcController(messages.messages, CacheAgeTest)
      val action = csrfAddToken(controller.showTaxCodeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val items = List(
        YouHaveToldUsItem("1150L", "Tax Code", "/foo"),
        YouHaveToldUsItem("YES", "Over 65", "/foo")
      )

      val expectedTaxCode = "1150L"
      val expectedAgeAnswer = "YES"

      val expectedNumberOfRows = 1 + items.size //Including header
      val actualTaxCode = parseHtml.getElementsByTag("tr").get(1).getElementsByTag("span").get(0).text()
      val actualAgeAnswer = parseHtml.getElementsByTag("tr").get(2).getElementsByTag("span").get(0).text()

      status shouldBe 200
      parseHtml.getElementsByTag("tr").size shouldBe expectedNumberOfRows
      actualTaxCode shouldBe expectedTaxCode
      actualAgeAnswer shouldBe expectedAgeAnswer
    }

    "return 200, with emtpy list of aggregate data" in {
      val controller = new QuickCalcController(messages.messages, CacheEmptyTest)
      val action = csrfAddToken(controller.showAgeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      status shouldBe 200
      parseHtml.getElementsByTag("tr").size shouldBe 0
    }
  }

  "Submit Age Form" should {

    "return 400, with current list of aggregate data and an error message for invalid answer" in {

    }

    "return 400, with empty list of aggregate data and an error message for invalid answer" in {

    }

    "return 303, with an answer \"No\" for not being Over 65 saved on the current list of aggregate data without Salary" in {

    }

    "return 303, with an answer \"Yes\" for being Over 65 saved on a new list of aggregate data" in {

    }

    "return 303, with an answer \"No\" for being Over 65 saved on the current list of aggregate data which contains all answered questions" in {

    }

  }

  "Show Salary Form" should {
    "return 200, with current list of aggregate data containing Tax Code: 1150L, \"YES\" for is not Over65, 20000 a Year for Salary" in {
      val controller = new QuickCalcController(messages.messages, CacheAllTest)
      val action = csrfAddToken(controller.showTaxCodeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val items = List(
        YouHaveToldUsItem("1150L", "Tax Code", "/foo"),
        YouHaveToldUsItem("YES", "Over 65", "/foo"),
        YouHaveToldUsItem("20000", "Per year", "/foo")
      )

      val expectedTaxCode = "1150L"
      val expectedAgeAnswer = "YES"
      val expectedSalary = "£20000"
      val expectedSalaryType = "Per year"

      val expectedNumberOfRows = 1 + items.size //Including header
      val actualTaxCode = parseHtml.getElementsByTag("tr").get(1).getElementsByTag("span").get(0).text()
      val actualAgeAnswer = parseHtml.getElementsByTag("tr").get(2).getElementsByTag("span").get(0).text()
      val actualSalary = parseHtml.getElementsByTag("tr").get(3).getElementsByTag("span").get(0).text()
      val actualySalaryType = parseHtml.getElementsByTag("tr").get(3).getElementsByTag("span").get(1).text()

      status shouldBe 200
      parseHtml.getElementsByTag("tr").size shouldBe expectedNumberOfRows
      actualTaxCode shouldBe expectedTaxCode
      actualAgeAnswer shouldBe expectedAgeAnswer
      actualSalary shouldBe expectedSalary
      actualySalaryType shouldBe expectedSalaryType
    }

    "return 200, with empty list of aggregate data" in {
      val controller = new QuickCalcController(messages.messages, CacheEmptyTest)
      val action = csrfAddToken(controller.showAgeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      status shouldBe 200
      parseHtml.getElementsByTag("tr").size shouldBe 0
    }

  }

  "Submit Salary Form" should {

    "return 400, with current list of aggregate data and an error message for invalid Salary" in {

    }

    "return 400, with empty list of aggregate data and an error message for invalid Salary" in {

    }

    "return 303, with new Salary data e.g. \"Yearly Salary £20000\" saved on the current list of aggregate data without Over 65 answer" in {

    }

    "return 303, with new Salary data e.g. \"Yearly Salary £20000\" saved on a new list of aggregate data" in {

    }

    "return 303, with new Salary data \"Yearly Salary £20000\" saved on the current list of aggregate data which contains all answered questions" in {

    }


  }

  "Show Result Page" should {

    "return 200, with current list of aggregate which contains all answers from previous questions" in {
      val controller = new QuickCalcController(messages.messages, CacheAllTest)
      val action = csrfAddToken(controller.showResult())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val items = List(
        YouHaveToldUsItem("1150L", "Tax Code", "/foo"),
        YouHaveToldUsItem("YES", "Over 65", "/foo"),
        YouHaveToldUsItem("20000", "Per year", "/foo")
      )

      val expectedTaxCode = "1150L"
      val expectedAgeAnswer = "YES"
      val expectedSalary = "£20000"
      val expectedSalaryType = "Per year"

      val expectedNumberOfRows = 1 + items.size //Including header
      val actualTableSize = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").size()
      val actualTaxCode = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(1).getElementsByTag("span").get(0).text()
      val actualAgeAnswer =  parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(2).getElementsByTag("span").get(0).text()
      val actualSalary =  parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(3).getElementsByTag("span").get(0).text()
      val actualySalaryType =  parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(3).getElementsByTag("span").get(1).text()

      status shouldBe 200
      actualTableSize shouldBe expectedNumberOfRows
      actualTaxCode shouldBe expectedTaxCode
      actualAgeAnswer shouldBe expectedAgeAnswer
      actualSalary shouldBe expectedSalary
      actualySalaryType shouldBe expectedSalaryType
    }

    "return 303, with current list of aggregate data and redirect to Tax Code Form if Tax Code is not provided" in {
      val controller = new QuickCalcController(messages.messages, CacheFinalNoTaxCodeTest)
      val action = csrfAddToken(controller.showResult())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val items = List(
        YouHaveToldUsItem("YES", "Over 65", "/foo"),
        YouHaveToldUsItem("20000", "Per year", "/foo")
      )

      val expectedAgeAnswer = "YES"
      val expectedSalary = "£20000"
      val expectedSalaryType = "Per year"

      val expectedNumberOfRows = 1 + items.size //Including header

      status shouldBe 303
    }

    "return 303, with current list of aggregate data and redirect to Age Form if no answer is provided for \"Are you Over 65?\"" in {

    }

    "return 303, with current list of aggregate data and redirect to Salary Form if Salary is not provided" in {

    }

    "return 303, with empty list of aggregate data and redirect to Tax Code Form" in {

    }


  }



}
