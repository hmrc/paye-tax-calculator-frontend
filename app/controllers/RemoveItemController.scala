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

package controllers

import config.AppConfig
import forms.forms.RemoveTaxCodeFormProvider
import models.QuickCalcAggregateInput
import play.api.data.Form
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents, Request, Result}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.ActionWithSessionId
import views.html.pages.RemoveItemView

import scala.concurrent.ExecutionContext


@Singleton
class RemoveItemController @Inject()(
                                         override val messagesApi:      MessagesApi,
                                         cache:                         QuickCalcCache,
                                         val controllerComponents:      MessagesControllerComponents,
                                         navigator:                     Navigator,
                                         removeItemView:                RemoveItemView,
                                         removeTaxCodeFormProvider:     RemoveTaxCodeFormProvider
                                       )(implicit val app:              AppConfig,
                                         implicit val executionContext: ExecutionContext)
                                          extends FrontendBaseController
                                          with I18nSupport
                                          with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: String => Form[Boolean] = option => removeTaxCodeFormProvider(option)

  def showRemoveItemForm(option: String): Action[AnyContent]=
    salaryRequired(cache,showRemoveTaxCodeFormTestable(option))

  private[controllers] def showRemoveTaxCodeFormTestable(option: String): ShowForm = { implicit request =>
    agg =>
      Ok(removeItemView(form(option), agg.additionalQuestionItems,option))
  }

  def submitRemoveItemForm(option: String): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form(option)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  removeItemView(formWithErrors, aggregate.additionalQuestionItems, option)
                )
              case None =>
                BadRequest(removeItemView(formWithErrors, Nil, option))
            },
          removeItemBoolean => {
            cache.fetchAndGetEntry().flatMap {
              case Some(aggregate) =>
                val updatedAggregate= if (removeItemBoolean) {
                  aggregate.copy(savedTaxCode = aggregate.savedTaxCode.map(_.copy(taxCode = None, gaveUsTaxCode = false)))
                } else {
                  aggregate
                }
                cache.save(updatedAggregate).map { _ =>
                  Redirect(
                    navigator.nextPageOrSummaryIfAllQuestionsAnswered(
                      updatedAggregate
                    ) {
                      routes.YouHaveToldUsNewController.summary
                    }
                  )
                }
            }
          }
        )
    }

  private def salaryRequired(
                                 cache: QuickCalcCache,
                                 furtherAction: Request[AnyContent] => QuickCalcAggregateInput => Result
                               ): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      cache.fetchAndGetEntry().map {
        case Some(aggregate) =>
          if (aggregate.savedSalary.isDefined)
            furtherAction(request)(aggregate)
          else
            Redirect(routes.SalaryController.showSalaryForm)
        case None =>
          Redirect(routes.SalaryController.showSalaryForm)
      }
    }

}