/*
 * Copyright 2024 HM Revenue & Customs
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

//  Added for POC , will be removed later
package utils

import javax.inject.Inject
import javax.xml.transform.stream.StreamSource
import play.api.Logging
import play.api.Environment

class DefaultResourceStreamResolver @Inject()(val environment: Environment) extends BaseResourceStreamResolver

trait BaseResourceStreamResolver extends Logging {
  val environment: Environment

  def resolvePath(filePath: String): StreamSource = {

    environment.resourceAsStream(filePath) match {
      case None =>
        logger.error("[ResourceStreamResolver] No resolver stream available")
        throw new RuntimeException("[ResourceStreamResolver] No resolver stream available")
      case Some(stream) =>
        logger.info("[ResourceStreamResolver] Valid input stream found")
        new StreamSource(stream)
    }
  }
}