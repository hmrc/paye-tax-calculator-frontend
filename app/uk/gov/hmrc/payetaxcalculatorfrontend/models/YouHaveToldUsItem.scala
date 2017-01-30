/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend.models

import play.api.data.Form

object UserToldUsAboutDetail {

  def userToldUsAboutTaxCode(userTaxCode: Form[UserTaxCode], url: String): List[YouHaveToldUsItem] = {

    val userToldUsAboutTaxCode = userTaxCode.fold(
      hasErrors = {
        _ =>
          println("======================Error")
          List.empty

      },
      success = {
        theUser =>
          if (!theUser.hasTaxCode) {
            List(YouHaveToldUsItem("1100L", "Tax Code", url))
          } else {
            List(YouHaveToldUsItem(userTaxCode.data("code"), "Tax Code", url))
          }
      }
    )

    userToldUsAboutTaxCode
  }

}

case class YouHaveToldUsItem(value: String, label: String, url: String)
case class UserTaxCode(hasTaxCode: Boolean, taxCode: Option[String])
