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

package testOnly.controllers

import config.AppConfig
import forms.FeatureSwitchForm
import models.FeatureSwitchModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import testOnly.views.html.FeatureSwitchView

import javax.inject.Inject

class FeatureSwitchController @Inject() (
  implicit val appConfig: AppConfig,
  mcc:                    MessagesControllerComponents,
  featureSwitch:          FeatureSwitchView)
    extends FrontendController(mcc)
    with I18nSupport {

  def featureSwitch: Action[AnyContent] = Action { implicit request =>
    Ok(
      featureSwitch(
        FeatureSwitchForm.form.fill(
          FeatureSwitchModel(
            welshTranslationEnabled = appConfig.features.welshTranslationFeature()
          )
        )
      )
    )
  }

  def submitFeatureSwitch: Action[AnyContent] = Action { implicit request =>
    FeatureSwitchForm.form
      .bindFromRequest()
      .fold(
        _ => Redirect(routes.FeatureSwitchController.featureSwitch),
        success = handleSuccess
      )
  }

  private def handleSuccess(model: FeatureSwitchModel): Result = {
    appConfig.features.welshTranslationFeature(model.welshTranslationEnabled)
    Redirect(routes.FeatureSwitchController.featureSwitch)
  }
}
