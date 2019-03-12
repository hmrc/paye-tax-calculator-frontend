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

import play.api.mvc.{Filter, RequestHeader, Result}
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.SessionIdSupport.hasSessionId
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport

import scala.concurrent.Future

class SessionIdFilter extends Filter with MicroserviceFilterSupport {
  def apply(next: (RequestHeader) => Future[Result])
           (rh: RequestHeader): Future[Result] = {
    if (hasSessionId(rh)) {
      next(rh)
    } else {
      next(addNewSessionIdToHeaders(rh))
    }
  }

  def addNewSessionIdToHeaders(request: RequestHeader): RequestHeader = {
    val newSessionId = s"session-${UUID.randomUUID().toString}"
    val newSessionIdHeader = HeaderNames.xSessionId -> newSessionId
    val newHeaders = request.headers.add(newSessionIdHeader)
    request.copy(headers = newHeaders)
  }
}