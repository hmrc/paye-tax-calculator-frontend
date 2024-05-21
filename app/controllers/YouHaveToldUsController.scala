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

package controllers

import config.AppConfig

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ActionWithSessionId, SalaryRequired}
import views.html.pages.YouHaveToldUsView

import scala.concurrent.ExecutionContext

@Singleton
class YouHaveToldUsController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  yourHaveToldUsView:            YouHaveToldUsView
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId with SalaryRequired{

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  def summary(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        aggregate =>
          if (aggregate.allQuestionsAnswered)
            Ok(yourHaveToldUsView(aggregate.youHaveToldUsItems()))
          else
            Redirect(navigator.redirectToNotYetDonePage(aggregate))
    )

}
