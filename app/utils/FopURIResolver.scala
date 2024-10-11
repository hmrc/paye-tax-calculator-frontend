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

//Added as a part of POC , will be removed later
package utils

import java.io.OutputStream
import java.net.URI

import javax.inject.Inject
import org.apache.xmlgraphics.io.{Resource, ResourceResolver}
import play.api.Environment

class DefaultFopURIResolver @Inject() (val environment: Environment) extends FopURIResolver

trait FopURIResolver extends ResourceResolver with BaseResourceStreamResolver {
  override val environment: Environment

  override def getOutputStream(uri: URI): OutputStream = ???

  override def getResource(uri: URI): Resource = {
    logger.info("[FopURIResolver] URI to convert to resource " + uri.toASCIIString)
    val resourcePath: String = uri.getPath.substring(uri.getPath.lastIndexOf("/pdf") + 1)

    new Resource(resolvePath(resourcePath).getInputStream)
  }
}
