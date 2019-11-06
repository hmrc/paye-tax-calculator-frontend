/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.config

import java.util.UUID

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.mvc._
import play.api.mvc.request.{Cell, RequestAttrKey}
import uk.gov.hmrc.http.{SessionKeys, HeaderNames => HMRCHeaderNames}

import scala.concurrent.{ExecutionContext, Future}

class SessionIdFilter(
                       override val mat: Materializer,
                       uuid: => UUID,
                       sessionCookieBaker: SessionCookieBaker,
                       implicit val ec: ExecutionContext
                     ) extends Filter {

  @Inject
  def this(mat: Materializer, ec: ExecutionContext, sessionCookieBaker: SessionCookieBaker) {
    this(mat, UUID.randomUUID(), sessionCookieBaker, ec)
  }

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    lazy val sessionId: String = s"session-$uuid"

    if (rh.session.get(SessionKeys.sessionId).isEmpty) {
      val headers = rh.headers.add(
        HMRCHeaderNames.xSessionId -> sessionId
      )

      val session = rh.session + (SessionKeys.sessionId -> sessionId)

      f(rh.withHeaders(headers).addAttr(RequestAttrKey.Session, Cell(session))).map {
        result =>

          val updatedSession = if (result.session(rh).get(SessionKeys.sessionId).isDefined) {
            result.session(rh)
          } else {
            result.session(rh) + (SessionKeys.sessionId -> sessionId)
          }

          result.withSession(updatedSession)
      }
    } else {
      f(rh)
    }
  }
}




//import java.util.UUID
//
//import akka.stream.Materializer
//
//import javax.inject.Inject
//import play.api.http.HeaderNames
//import play.api.mvc.{Cookies, Filter, RequestHeader, Result, Session}
//import uk.gov.hmrc.http.{SessionKeys, HeaderNames => HMRCHeaderNames}
//
//import akka.stream.Materializer
//
//import scala.concurrent.{ExecutionContext, Future}
//
//class SessionIdFilter (
//                        override val mat: Materializer,
//                        uuid: => UUID,
//                        implicit val ec: ExecutionContext
//                      ) extends Filter {
//
//  @Inject
//  def this(mat: Materializer, ec: ExecutionContext) {
//    this(mat, UUID.randomUUID(), ec)
//  }
//
//  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
//
//    lazy val sessionId: String = s"session-$uuid"
//
//    if (rh.session.get(SessionKeys.sessionId).isEmpty) {
//
//      val cookies: String = {
//
//        val session: Session =
//          rh.session + (SessionKeys.sessionId -> sessionId)
//
//        val cookies =
//          rh.cookies ++ Seq(Session.encodeAsCookie(session))
//
//        Cookies.encodeCookieHeader(cookies.toSeq)
//      }
//
//      val headers = rh.headers.add(
//        HMRCHeaderNames.xSessionId -> sessionId,
//        HeaderNames.COOKIE -> cookies
//      )
//
//      f(rh.copy(headers = headers)).map {
//        result =>
//
//          val cookies =
//            Cookies.fromSetCookieHeader(result.header.headers.get(HeaderNames.SET_COOKIE))
//
//          val session = Session.decodeFromCookie(cookies.get(Session.COOKIE_NAME)).data
//            .foldLeft(rh.session) {
//              case (m, n) =>
//                m + n
//            }
//
//          result.withSession(session + (SessionKeys.sessionId -> sessionId))
//      }
//    } else {
//      f(rh)
//    }
//  }
//}

//import java.util.UUID
//import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
//import play.api.mvc.{Filter, RequestHeader, Result}
//import uk.gov.hmrc.payetaxcalculatorfrontend.utils.SessionIdSupport.hasSessionId
//
//import scala.concurrent.Future
//
//class SessionIdFilter (override val mat: Materializer,
//                        implicit val ec: ExecutionContext) extends Filter {
//  def apply(next: (RequestHeader) => Future[Result])
//           (rh: RequestHeader): Future[Result] = {
//    if (hasSessionId(rh)) {
//      next(rh)
//    } else {
//      next(addNewSessionIdToHeaders(rh))
//    }
//  }
//
//  def addNewSessionIdToHeaders(request: RequestHeader): RequestHeader = {
//    val newSessionId = s"session-${UUID.randomUUID().toString}"
//    val newSessionIdHeader = HeaderNames.xSessionId -> newSessionId
//    val newHeaders = request.headers.add(newSessionIdHeader)
//    request.copy(headers = newHeaders)
//  }
//}