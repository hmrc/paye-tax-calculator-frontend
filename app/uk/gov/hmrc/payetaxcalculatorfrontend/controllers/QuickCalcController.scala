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

package uk.gov.hmrc.payetaxcalculatorfrontend.controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.payetaxcalculatorfrontend.models._
import uk.gov.hmrc.payetaxcalculatorfrontend.utils.ActionWithSessionId
import uk.gov.hmrc.payetaxcalculatorfrontend.views.html.quickcalc.quick_calc_form
import uk.gov.hmrc.play.frontend.controller.FrontendController

@Singleton
class QuickCalcController @Inject() (override val messagesApi: MessagesApi) extends FrontendController with I18nSupport {


  def showForm() = ActionWithSessionId { implicit request =>
    Ok(quick_calc_form(List.empty))
  }

  def passTaxCode(url: String) = ActionWithSessionId { implicit request =>

    val userTaxCode = AllForms.userTaxCodeForm.bindFromRequest

    Ok(quick_calc_form(UserToldUsAboutDetail.userToldUsAboutTaxCode(userTaxCode, url)))

  }

}
