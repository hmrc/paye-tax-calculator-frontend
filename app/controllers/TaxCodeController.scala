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
import forms.UserTaxCodeFormProvider

import javax.inject.{Inject, Singleton}
import models.{QuickCalcAggregateInput, UserTaxCode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, DefaultTaxCodeProvider}
import views.html.pages.TaxCodeView

import scala.concurrent.ExecutionContext

@Singleton
class TaxCodeController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  taxCodeView:                   TaxCodeView,
  userTaxCodeFormProvider:       UserTaxCodeFormProvider,
  defaultTaxCodeProvider: DefaultTaxCodeProvider
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[UserTaxCode] = userTaxCodeFormProvider()
  def showTaxCodeForm: Action[AnyContent] =
    salaryRequired(
      cache, { implicit request => agg =>
        val filledForm = agg.savedTaxCode
          .map { s =>
            form.fill(s)
          }
          .getOrElse(form)
        Ok(taxCodeView(filledForm, agg.youHaveToldUsItems, defaultTaxCodeProvider.defaultUkTaxCode))
      }
    )

  def submitTaxCodeForm(): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  taxCodeView(formWithErrors, aggregate.youHaveToldUsItems, defaultTaxCodeProvider.defaultUkTaxCode)
                )
              case None => BadRequest(taxCodeView(formWithErrors, Nil, defaultTaxCodeProvider.defaultUkTaxCode))
            },
          (newTaxCode: UserTaxCode) => {
            val updatedAggregate = cache
              .fetchAndGetEntry()
              .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
              .map(agg =>
                agg.copy(
                  savedTaxCode = Some(
                    UserTaxCode(
                      newTaxCode.taxCode.isDefined,
                      Some(
                        newTaxCode.taxCode
                          .getOrElse(defaultTaxCodeProvider.defaultUkTaxCode)
                      )
                    )
                  )
                )
              )

            updatedAggregate.flatMap { updatedAgg =>
              val agg = {
                if(appConfig.features.newScreenContentFeature()){
                  updatedAgg
                }else {
                  if (newTaxCode.taxCode.isDefined)
                    updatedAgg.copy(savedScottishRate = None)
                  else updatedAgg
                }
              }
              cache
                .save(agg)
                .map(_ =>
                  if(appConfig.features.newScreenContentFeature()){
                    if(newTaxCode.taxCode.isEmpty) {
                      Redirect(routes.YouHaveToldUsNewController.summary)
                    } else {
                      Redirect(
                        navigator
                          .nextPageOrSummaryIfAllQuestionsAnswered(agg) {
                            routes.YouHaveToldUsController.summary
                          }
                      )
                    }
                  }else {
                    if (newTaxCode.taxCode.isEmpty) {
                      Redirect(
                        routes.ScottishRateController.showScottishRateForm
                      )
                    } else {
                      Redirect(
                        navigator
                          .nextPageOrSummaryIfAllQuestionsAnswered(agg) {
                            routes.YouHaveToldUsController.summary
                          }
                      )
                    }
                  }
              )
            }
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
            Redirect(routes.SalaryController.showSalaryForm)
        case None =>
          Redirect(routes.SalaryController.showSalaryForm)
      }
    }

}
