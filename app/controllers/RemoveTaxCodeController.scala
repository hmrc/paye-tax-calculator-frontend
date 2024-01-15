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
import models.{QuickCalcAggregateInput, UserTaxCode}
import play.api.data.Form

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents, Request, Result}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.ActionWithSessionId
import views.html.pages.RemoveTaxCodeView

import scala.concurrent.ExecutionContext


@Singleton
class RemoveTaxCodeController @Inject()(
                                         override val messagesApi:      MessagesApi,
                                         cache:                         QuickCalcCache,
                                         val controllerComponents:      MessagesControllerComponents,
                                         navigator:                     Navigator,
                                         removeTaxCodeView:             RemoveTaxCodeView,
                                         removeTaxCodeFormProvider:     RemoveTaxCodeFormProvider
                                       )(implicit val app:              AppConfig,
                                         implicit val executionContext: ExecutionContext)
                                          extends FrontendBaseController
                                          with I18nSupport
                                          with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[UserTaxCode] = removeTaxCodeFormProvider()

  def showRemoveTaxCodeForm(): Action[AnyContent]=
    salaryRequired(cache,showRemoveTaxCodeFormTestable)

  private[controllers] def showRemoveTaxCodeFormTestable: ShowForm = { implicit request => agg =>
    val filledForm = agg.savedTaxCode
      .map{ s =>
        form.fill(s)
      }
      .getOrElse(form)

    Ok(removeTaxCodeView(filledForm, agg.additionalQuestionItems))
  }

  def submitRemoveTaxCodeForm(): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  removeTaxCodeView(formWithErrors, aggregate.additionalQuestionItems)
                )
              case None =>
                BadRequest(removeTaxCodeView(formWithErrors, Nil))
            },
          userTaxCode => {
            println("WH User Tax Code" + userTaxCode)
            cache.fetchAndGetEntry().flatMap {
              case Some(aggregate) =>
                val updatedAggregate = {
                  aggregate.copy(savedTaxCode = Some(userTaxCode))
                }
                println("WH + Updated Aggregate" + updatedAggregate)
                cache.save(updatedAggregate).map { _ =>
                  Redirect(
                    navigator.nextPageOrSummaryIfAllQuestionsAnswered(
                      updatedAggregate
                    ) {
                      routes.YouHaveToldUsNewController.summary
                    }
                  )
                }
              case None =>
                cache
                  .save(
                    QuickCalcAggregateInput.newInstance
                      .copy(savedTaxCode = Some(userTaxCode))
                  ).map { _ =>
                  Redirect(routes.YouHaveToldUsNewController.summary)
                }
            }
          }
        )
    }

  private def salaryRequired[T](
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