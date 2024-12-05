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

package forms

import mappings.CustomFormatters
import javax.inject.Inject
import models.Salary
import play.api.data.Form
import play.api.data.Forms.{mapping, of, optional}
import play.api.data.format.Formats._

class SalaryFormProvider @Inject() () {

  def apply(): Form[Salary] = Form(
    mapping(
      "amount"               -> of(CustomFormatters.salaryValidation),
      "amountYearly"         -> optional(of[BigDecimal]),
      "previousAmountYearly" -> optional(of[BigDecimal]),
      "period"               -> of(SalaryPeriodForm.salaryPeriodFormatter),
      "how-many-a-week"      -> optional(of[BigDecimal]),
      "monthlyAmount"        -> optional(of[BigDecimal])
    )(Salary.apply)(Salary.unapply)
  )
}
