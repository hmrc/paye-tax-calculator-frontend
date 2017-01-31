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

package uk.gov.hmrc.payetaxcalculatorfrontend.model

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import uk.gov.hmrc.payeestimator.services.TaxCalculatorHelper


case class UserTaxCode(hasTaxCode: Boolean, taxCode: Option[String])

object UserTaxCode extends TaxCalculatorHelper {

  implicit val format = Json.format[UserTaxCode]

  val form = Form(
    mapping(
      "hasTaxCode" -> boolean,
      "code" -> optional(text)
    )(UserTaxCode.apply)(UserTaxCode.unapply).verifying(
      aboutTaxCode =>
        if (aboutTaxCode.hasTaxCode) isValidTaxCode(aboutTaxCode.taxCode.getOrElse("").trim) else true
    ))
}