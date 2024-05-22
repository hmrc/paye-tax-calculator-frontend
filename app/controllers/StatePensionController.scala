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
import forms.StatePensionFormProvider

import javax.inject.{Inject, Singleton}
import models.{QuickCalcAggregateInput, StatePension, UserTaxCode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, SalaryRequired}
import views.html.pages.StatePensionView

import scala.concurrent.ExecutionContext

@Singleton
class StatePensionController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  statePensionView:              StatePensionView,
  statePensionFormProvider:      StatePensionFormProvider,
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId with SalaryRequired{

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[StatePension] = statePensionFormProvider()

  def showStatePensionForm(): Action[AnyContent] =
    salaryRequired(cache, showStatePensionFormTestable)

  private[controllers] def showStatePensionFormTestable: ShowForm = { implicit request => agg =>
    val filledForm = agg.savedIsOverStatePensionAge
      .map { s =>
        form.fill(s)
      }
      .getOrElse(form)

    Ok(statePensionView(filledForm, agg.youHaveToldUsItems()))
  }

  def submitStatePensionForm(): Action[AnyContent] =
    validateAcceptWithSessionId().async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  statePensionView(formWithErrors, aggregate.youHaveToldUsItems())
                )
              case None =>
                BadRequest(statePensionView(formWithErrors, Nil))
            },
          userAge =>
            cache.fetchAndGetEntry().flatMap {
              case Some(aggregate) =>
                val updatedAggregate = {
                  if (appConfig.features.newScreenContentFeature()) {
                    aggregate.copy(
                      savedIsOverStatePensionAge = Some(userAge),
                      savedTaxCode               = Some(UserTaxCode(gaveUsTaxCode = false, None))
                    )
                  } else {
                    aggregate.copy(savedIsOverStatePensionAge = Some(userAge))
                  }
                }
                cache.save(updatedAggregate).map { _ =>
                  Redirect(
                    navigator.nextPageOrSummaryIfAllQuestionsAnswered(
                      updatedAggregate
                    ) {
                      if (appConfig.features.newScreenContentFeature()) {
                        routes.YouHaveToldUsNewController.summary
                      } else {
                        routes.TaxCodeController.showTaxCodeForm
                      }
                    }()
                  )
                }
              case None =>
                cache
                  .save(
                    QuickCalcAggregateInput.newInstance
                      .copy(savedIsOverStatePensionAge = Some(userAge))
                  )
                  .map { _ =>
                    if (appConfig.features.newScreenContentFeature()) {
                      Redirect(routes.YouHaveToldUsNewController.summary)
                    } else {
                      Redirect(routes.TaxCodeController.showTaxCodeForm)
                    }
                  }
            }
        )
    }

}
