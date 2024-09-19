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

package config

import org.apache.pekko.stream.Materializer

import javax.inject.Inject
import play.api.mvc.{Result, _}

import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.Future


// $COVERAGE-OFF$
class CsrfBypassFilter @Inject() (implicit val mat: Materializer) extends Filter {

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] =
    f(filteredHeaders(rh))

  private[config] def filteredHeaders(
    rh:  RequestHeader,
    now: () => ZonedDateTime = () => ZonedDateTime.now(ZoneId.of("UTC"))
  ): RequestHeader =
    rh.withHeaders(rh.headers.add("Csrf-Token" -> "nocheck"))

}
// $COVERAGE-ON$
