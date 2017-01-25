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

object Forms {

  val QuickCalcUserInputForm : Form[QuickCalcUserInput] =
    Form(mapping(
      "taxCode" -> nonEmptyText,
      "isStatePensionAge" -> boolean,
      "taxYear" -> nonEmptyText,
      "grossPay" -> optional(bigDecimal),
      "payPeriod" -> nonEmptyText,
      "hourlyRate" -> optional(bigDecimal),
      "hoursPerWeek" -> optional(number)
    )(QuickCalcUserInput.apply)(QuickCalcUserInput.unapply))

}

case class QuickCalcUserInput(taxCode: String,
                              isStatePensionAge: Boolean,
                              taxYear: String,
                              grossPay: Option[BigDecimal],
                              payPeriod: String,
                              hourlyRate: Option[BigDecimal],
                              hoursPerWeek: Option[Int])
