package uk.gov.hmrc.payetaxcalculatorfrontend.utils

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.OneAppPerSuite
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}
import uk.gov.hmrc.payetaxcalculatorfrontend.config.SessionIdFilter
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SessionIdFilterSpec extends UnitSpec with OneAppPerSuite with ScalaFutures {

  val testSessionIdFilter = new SessionIdFilter

  "SessionIdFilter" should {
    "add a newly generated session-id to a request that didn't have one before" in new Setup {
      executeFilteredAction(FakeRequest()) { rh =>
        if(!sessionIdFoundInHeaders(rh)) {
          fail("session-id not added to headers as expected")
        }
      }
    }
    "not modify headers if session-id was present already in headers" in new Setup {
      val existingSessionId = "foo"
      executeFilteredAction(FakeRequest().withHeaders(HeaderNames.xSessionId -> existingSessionId)) { rh =>
        if (!sessionIdFoundInHeaders(rh)) {
          fail("session-id removed from headers")
        } else if (!sessionIdEquals(existingSessionId, rh)) {
          fail(s"session-id was modified, expected: $existingSessionId but got: ${rh.headers.headers.map(_._2)}")
        }
      }
    }
    "not modify headers and session if session-id was present already in session" in new Setup {
      val existingSessionId = "foo"
      executeFilteredAction(FakeRequest().withSession(SessionKeys.sessionId -> existingSessionId)) { rh =>
        if (!sessionIdFoundInSession(rh)) {
          fail("session-id not longer found in session")
        } else if (sessionIdFoundInHeaders(rh)) {
          fail("session-id found in headers but shouldn't have been added as it was already in session")
        }
      }
    }
  }

  trait Setup extends Results {
    def action(assertion: RequestHeader => Unit): RequestHeader => Future[Result] = { rh =>
      assertion(rh)
      Future.successful(Ok)
    }

    def executeFilteredAction(rh: RequestHeader)(assertion:  RequestHeader => Unit) =
      testSessionIdFilter(action(assertion))(rh).futureValue

    def getSessionIdFromHeaders(rh: RequestHeader): Option[String] = rh.headers.get(HeaderNames.xSessionId)

    def sessionIdFoundInHeaders(rh: RequestHeader): Boolean = getSessionIdFromHeaders(rh).isDefined

    def sessionIdFoundInSession(rh: RequestHeader): Boolean = rh.session.get(SessionKeys.sessionId).isDefined

    def sessionIdEquals(expectedValue: String, rh: RequestHeader): Boolean = {
      rh.headers.headers
        .filter { case (k, v) => k == HeaderNames.xSessionId }
        .forall { case (k, v) => v == expectedValue }
    }
  }
}
