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

package utils

import play.api.i18n.Messages
//Added as a part of POC , will be removed later
object XSLScalaBridge {
  def apply(messages: Messages): XSLScalaBridge = new XSLScalaBridge(messages)
}

class XSLScalaBridge private (messages: Messages) {
  def getMessagesText(key: String): String = messages(key)

  def getMessagesTextWithParameter(
    key:       String,
    parameter: String
  ): String = messages(key, parameter)

  def getLang(): String = messages.lang.language

}
