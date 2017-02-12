package uk.gov.hmrc.payetaxcalculatorfrontend.controllers

import akka.stream.Materializer
import org.jsoup.Jsoup
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.filters.csrf._
import uk.gov.hmrc.payetaxcalculatorfrontend.model._
import uk.gov.hmrc.payetaxcalculatorfrontend.setup.QuickCalcCacheSetup._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

class QuickCalcControllerSpec() extends UnitSpec with Results with OneAppPerSuite {

  val appInjector = app.injector

  val csrfAddToken = appInjector.instanceOf[CSRFAddToken]

  implicit val materializer = appInjector.instanceOf[Materializer]

  implicit val request = FakeRequest()
    .withHeaders(HeaderNames.xSessionId -> "test")

  implicit val messages = Messages(Lang.defaultLang, appInjector.instanceOf[MessagesApi])


  "Redirect to Tax Code Form" should {
    "return 303" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val result = controller.redirectToTaxCodeForm().apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      status shouldBe 303
    }
  }

  "Show Tax Code Form" should {
    "return 200 and an empty list of aggregate data if no aggregate data is present" in {

      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val action = csrfAddToken(controller.showTaxCodeForm())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      status shouldBe 200
      parseHtml.getElementsByTag("tr").size shouldBe 0
    }

    "return 200 and a list of current aggregate data containing Tax Code: 1150L" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
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
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true, Some("110")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
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
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true, Some("110")))
      val action = await(csrfAddToken(controller.submitTaxCodeForm()))
      val result = action(request.withFormUrlEncodedBody(formTax.data.toSeq: _*)).withSession(request.session + (SessionKeys.sessionId -> "test-tax"))
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

    "return 303, with Tax Code Form submission, current list of aggregate and redirect to Is Over State Pension Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeAndIsOverStatePension)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true, Some("K425")))
      val postAction = await(csrfAddToken(controller.submitTaxCodeForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-tax")

      val status = postResult.header.status
      val actualRedirectUri = redirectLocation(postResult).get

      val expectedRedirectUri = "/paye-tax-calculator/quick-calculation/age"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, with Tax Code Form submission, new list of aggregate and redirect to Is Over State Pension Page" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true, Some("K425")))
      val postAction = await(csrfAddToken(controller.submitTaxCodeForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-tax")

      val status = postResult.header.status
      val actualRedirectUri = redirectLocation(postResult).get

      val expectedRedirectUri = "/paye-tax-calculator/quick-calculation/age"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, with Tax Code form submission, the complete list of aggregate data and redirect to Summary Result Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
      val formTax = UserTaxCode.form.fill(UserTaxCode(true, Some("K425")))
      val postAction = await(csrfAddToken(controller.submitTaxCodeForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formTax.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-tax")

      val status = postResult.header.status
      val actualRedirectUri = redirectLocation(postResult).get

      val expectedRedirectUri = "/paye-tax-calculator/quick-calculation/summary-result"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }
  }

  "Show Age Form" should {
    "return 200, with current list of aggregate data containing Tax Code: 1150L and \"YES\" for is not Over65" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeAndIsOverStatePension)
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
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
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
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
      val postAction = await(csrfAddToken(controller.submitAgeForm()))

      val postResult = postAction(request
        .withSession(SessionKeys.sessionId -> "test-age"))

      val status = postResult.header.status

      val responseBody = contentAsString(postResult)
      val parseHtml = Jsoup.parse(responseBody)

      val actualTableSize = parseHtml.getElementsByTag("tr").size()
      val actualErrorMessage = parseHtml.getElementsByClass("error-notification").text()

      status shouldBe 400

    }

    "return 400, with empty list of aggregate data and an error message for invalid answer" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
      val formAge = OverStatePensionAge.form
      val postAction = await(csrfAddToken(controller.submitAgeForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formAge.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-age")

      val status = postResult.header.status
    }

    "return 303, with an answer \"No\" for not being State Pension Age saved on the current list of aggregate data without Salary and redirect to Salary Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
      val formAge = OverStatePensionAge.form.fill(OverStatePensionAge(false))
      val postAction = await(csrfAddToken(controller.submitAgeForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formAge.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-age")

      val status = postResult.header.status
      val actualRedirectUri = redirectLocation(postResult).get

      val expectedRedirectUri = "/paye-tax-calculator/quick-calculation/salary"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, with an answer \"Yes\" for being Over 65 saved on a new list of aggregate data and redirect Salary Page" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formAge = OverStatePensionAge.form.fill(OverStatePensionAge(true))
      val postAction = await(csrfAddToken(controller.submitAgeForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formAge.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-age")

      val status = postResult.header.status
      val actualRedirectUri = redirectLocation(postResult).get

      val expectedRedirectUri = "/paye-tax-calculator/quick-calculation/salary"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

    "return 303, with an answer \"No\" for being Over 65 saved on the current list of aggregate data which contains all answered questions and redirect to Salary Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
      val formAge = OverStatePensionAge.form.fill(OverStatePensionAge(false))
      val postAction = await(csrfAddToken(controller.submitAgeForm()))

      val postResult = postAction(request
        .withFormUrlEncodedBody(formAge.data.toSeq: _*))
        .withSession(SessionKeys.sessionId -> "test-age")

      val status = postResult.header.status
      val actualRedirectUri = redirectLocation(postResult).get

      val expectedRedirectUri = "/paye-tax-calculator/quick-calculation/summary-result"

      status shouldBe 303
      actualRedirectUri shouldBe expectedRedirectUri
    }

  }

  "Show Salary Form" should {
    "return 200, with current list of aggregate data containing Tax Code: 1150L, \"YES\" for is not Over65, 20000 a Year for Salary" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
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
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
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
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeAndIsOverStatePension)
      val formSalary = Salary.form
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      status shouldBe 400
    }

    "return 400, with empty list of aggregate data and an error message for invalid Salary" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeAndIsOverStatePension)
      val formSalary = Salary.form
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      status shouldBe 400

    }

    "return 303, with new Salary data e.g. \"Yearly Salary £20000\" saved on the current list of aggregate data without Over 65 answer and redirect to Over State Pension Age" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCode)
      val formSalary = Salary.form.fill(Yearly(20000))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/tax-code"
      val actualRedirect = redirectLocation(postResult).get


      status shouldBe 303
    }

    "return 303, with new Salary data e.g. \"Yearly Salary £20000\" saved on a new list of aggregate data and redirect to Tax Code Page" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val formSalary = Salary.form.fill(Yearly(20000))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))

      val postResult = postAction(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/tax-code"
      val actualRedirect = redirectLocation(postResult).get

      status shouldBe 303
    }

    "return 303, with new Salary data \"Yearly Salary £20000\" saved on the complete list of aggregate data and redirect to Summary Result Page" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
      val formSalary = Salary.form.fill(Yearly(20000))
      val postAction = await(csrfAddToken(controller.submitSalaryForm()))
      val postResult = postAction(request.withFormUrlEncodedBody(formSalary.data.toSeq:_*)
        .withSession(SessionKeys.sessionId -> "test-salary"))

      val status = postResult.header.status

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/summary-result"
      val actualRedirect = redirectLocation(postResult).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }
  }

  "Show Result Page" should {
    "return 200, with current list of aggregate which contains all answers from previous questions" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeIsOverStatePensionAndSalary)
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
      val actualAgeAnswer = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(2).getElementsByTag("span").get(0).text()
      val actualSalary = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(3).getElementsByTag("span").get(0).text()
      val actualySalaryType = parseHtml.getElementsByTag("table").get(6).getElementsByTag("tr").get(3).getElementsByTag("span").get(1).text()

      status shouldBe 200
      actualTableSize shouldBe expectedNumberOfRows
      actualTaxCode shouldBe expectedTaxCode
      actualAgeAnswer shouldBe expectedAgeAnswer
      actualSalary shouldBe expectedSalary
      actualySalaryType shouldBe expectedSalaryType
    }

    "return 303, with current list of aggregate data and redirect to Tax Code Form if Tax Code is not provided" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnNoTaxCodeButAnswerEverythingElse)
      val action = csrfAddToken(controller.showResult())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val actualRedirect = redirectLocation(result).get

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/tax-code"

      status shouldBe 303
    }

    "return 303, with current list of aggregate data and redirect to Age Form if no answer is provided for \"Are you Over 65?\"" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnNoAgeButAnswerEverythingElse)
      val action = csrfAddToken(controller.showResult())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val actualRedirect = redirectLocation(result).get

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/age"

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with current list of aggregate data and redirect to Salary Form if Salary is not provided" in {
      val controller = new QuickCalcController(messages.messages, cacheReturnTaxCodeAndIsOverStatePension)
      val action = csrfAddToken(controller.showResult())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val actualRedirect = redirectLocation(result).get
      val expectedRedirect = "/paye-tax-calculator/quick-calculation/salary"

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }

    "return 303, with empty list of aggregate data and redirect to Tax Code Form" in {
      val controller = new QuickCalcController(messages.messages, cacheEmpty)
      val action = csrfAddToken(controller.showResult())
      val result = action.apply(request)
      val status = result.header.status
      val responseBody = contentAsString(result)
      val parseHtml = Jsoup.parse(responseBody)

      val expectedRedirect = "/paye-tax-calculator/quick-calculation/tax-code"

      val actualRedirect = redirectLocation(result).get

      status shouldBe 303
      actualRedirect shouldBe expectedRedirect
    }
  }
}
