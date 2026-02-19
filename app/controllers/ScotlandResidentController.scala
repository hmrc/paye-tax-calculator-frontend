/*
 * Copyright 2026 HM Revenue & Customs
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
import forms.LiveInScotlandFormProvider
import models.{QuickCalcAggregateInput, ScottishResident}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ActionWithSessionId, SalaryRequired}
import views.html.pages.ScottishResidentView
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ScotlandResidentController @Inject() (
  override val messagesApi: MessagesApi,
  cache: QuickCalcCache,
  val controllerComponents: MessagesControllerComponents,
  navigator: Navigator,
  scottishResidentView: ScottishResidentView,
  liveInScotlandFormProvider: LiveInScotlandFormProvider
)(implicit val appConfig: AppConfig, val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId
    with SalaryRequired {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[ScottishResident] = liveInScotlandFormProvider()

  def showScottishResidentForm(): Action[AnyContent] =
    salaryRequired(cache, showScottishResidentFormTestable)


  private[controllers] def showScottishResidentFormTestable: ShowForm = {
    implicit request =>
      agg =>
        val filledForm = agg.savedIsScottishResident
          .map { s =>
            form.fill(s)
          }
          .getOrElse(form)
        Ok(scottishResidentView(filledForm, agg.youHaveToldUsItems()))
  }

  def submitScottishResidentForm(): Action[AnyContent] =
    validateAcceptWithSessionId().async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  scottishResidentView(formWithErrors, aggregate.youHaveToldUsItems())
                )
              case None =>
                BadRequest(scottishResidentView(formWithErrors, Nil))
            },
          resident =>
            cache.fetchAndGetEntry().flatMap {
              case Some(aggregate) =>
                val updatedAggregate = {
                  aggregate.copy(savedIsScottishResident = Some(resident))
                }
                cache.save(updatedAggregate).map { _ =>
                  if(aggregate.savedIsScottishResident.exists(_.isScottishResident == true)) {
                    Redirect(
                      routes.ScottishWinterFuelPaymentsController.showScottishWinterFuelPayments()
                    )
                  }
                  else Redirect(
                    routes.WinterFuelPaymentsController.showWinterFuelPayments()
                  )
                    
                }
              case None =>
                cache
                .save(QuickCalcAggregateInput.newInstance
                .copy(savedIsScottishResident = Some(resident))
                )
                .map { _ =>
                  Redirect(routes.YouHaveToldUsNewController.summary())
                }
            }
        )


    }
}