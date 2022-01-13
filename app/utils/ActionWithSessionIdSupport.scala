/*
 * Copyright 2022 HM Revenue & Customs
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

package utils


import play.api.mvc._
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}
import utils.ActionWithSessionIdSupport._

import scala.concurrent.{ExecutionContext, Future}

/*
 * These utils provide session-id that is required by http-caching-client / keystore.
 * Normally you'd get session-id for free if you use authenticated actions but we don't
 * in this project.
 *
 * SessionIdFilter will add a header with a newly generated session-id and
 * ActionWithSessionId will make sure it stays saved in the session.
 */

object ActionWithSessionIdSupport {

  def maybeSessionId(rh: RequestHeader): Option[String] =
    rh.session.get(SessionKeys.sessionId).orElse(rh.headers.get(HeaderNames.xSessionId))
}

trait ActionWithSessionId {
  outer =>
  def parser: BodyParser[AnyContent]

  def executionContext: ExecutionContext

  def validateAcceptWithSessionId: ActionBuilder[Request, AnyContent] =
    new ActionBuilder[Request, AnyContent] {

      implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

      def invokeBlock[A](
        request: Request[A],
        block:   (Request[A]) => Future[Result]
      ): Future[Result] =
        maybeSessionId(request)
          .map { sessionId =>
            block(request).map(addSessionIdToSession(request, sessionId))
          }
          .getOrElse {
            throw SessionIdNotFoundException()
          }

      def addSessionIdToSession[A](
        request:   Request[A],
        sessionId: String
      )(result:    Result
      ): Result =
        result.withSession(request.session + (SessionKeys.sessionId -> sessionId))

      override def parser: BodyParser[AnyContent] = outer.parser

      override protected def executionContext: ExecutionContext = outer.executionContext
    }
}

case class SessionIdNotFoundException()
    extends Exception(
      "Session id not found in headers or session as expected. Have you enabled SessionIdFilter?"
    )
