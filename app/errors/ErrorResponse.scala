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

package errors

import play.api.libs.json._

case class ErrorResponse(
                          httpStatusCode: Int,
                          error:          String)

trait ServiceResponseError extends Product with Serializable {
  def message: String
}

case class MalformedRequest(message: String = "Malformed request") extends ServiceResponseError
case class MongoDBError(message:     String) extends ServiceResponseError // logged at repository level
case class MongoDBNoResults(message: String) extends ServiceResponseError

case object NoSessionException extends Exception("Could not find sessionId in HeaderCarrier")

object ErrorResponse {
  implicit val writes: Writes[ErrorResponse] = Json.writes[ErrorResponse]
  implicit val reads:  Reads[ErrorResponse]  = Json.reads[ErrorResponse]
}
