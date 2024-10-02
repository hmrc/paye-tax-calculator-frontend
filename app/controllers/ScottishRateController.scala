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
import forms.ScottishRateFormProvider

import javax.inject.{Inject, Singleton}
import models.{QuickCalcAggregateInput, ScottishRate, UserTaxCode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.QuickCalcCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, DefaultTaxCodeProvider, SalaryRequired}
import views.html.pages.ScottishRateView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScottishRateController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  scottishRateView:              ScottishRateView,
  scottishRateFormProvider:      ScottishRateFormProvider,
  defaultTaxCodeProvider:        DefaultTaxCodeProvider
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId
    with SalaryRequired {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[ScottishRate] = scottishRateFormProvider()

  def showScottishRateForm(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        agg => {
          val filledForm = agg.savedScottishRate
            .map { s =>
              form.fill(s)
            }
            .getOrElse(form)
          Ok(scottishRateView(filledForm, agg.youHaveToldUsItems()))
        }
    )

  def submitScottishRateForm(): Action[AnyContent] =
    validateAcceptWithSessionId().async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(scottishRateView(formWithErrors, Nil))),
          scottish => {
            val updatedAggregate =
              cache
                .fetchAndGetEntry()
                .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
                .map(
                  _.copy(
                    savedScottishRate = if (scottish.payScottishRate.isDefined) {
                      Some(ScottishRate(scottish.payScottishRate))
                    } else {
                      None
                    }
                  )
                )
            updatedAggregate
              .map(cache.save)
              .map(_ => Redirect(routes.YouHaveToldUsNewController.summary))
          }
        )
    }

}
