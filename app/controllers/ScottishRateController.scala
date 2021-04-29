/*
 * Copyright 2021 HM Revenue & Customs
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
import utils.ActionWithSessionId
import views.html.pages.ScottishRateView

import scala.concurrent.ExecutionContext

@Singleton
class ScottishRateController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  scottishRateView:              ScottishRateView,
  scottishRateFormProvider:      ScottishRateFormProvider
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

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
          Ok(scottishRateView(filledForm, agg.youHaveToldUsItems))
        }
    )

  def submitScottishRateForm(): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache
              .fetchAndGetEntry()
              .map {
                case Some(aggregate) => aggregate.youHaveToldUsItems
                case None            => Nil
              }
              .map(itemList => BadRequest(scottishRateView(formWithErrors, itemList))),
          scottish => {
            val taxCode =
              if (scottish.payScottishRate)
                UserTaxCode.defaultScottishTaxCode
              else
                UserTaxCode.defaultUkTaxCode

            val updatedAggregate = cache
              .fetchAndGetEntry()
              .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
              .map(
                _.copy(
                  savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some(taxCode))),
                  savedScottishRate = Some(ScottishRate(scottish.payScottishRate))
                )
              )
            updatedAggregate
              .map(cache.save)
              .map(_ => Redirect(routes.YouHaveToldUsController.summary()))
          }
        )
    }

  private def salaryRequired[T](
    cache:         QuickCalcCache,
    furtherAction: Request[AnyContent] => QuickCalcAggregateInput => Result
  ): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      cache.fetchAndGetEntry().map {
        case Some(aggregate) =>
          if (aggregate.savedSalary.isDefined)
            furtherAction(request)(aggregate)
          else
            Redirect(routes.SalaryController.showSalaryForm())
        case None =>
          Redirect(routes.SalaryController.showSalaryForm())
      }
    }

}
