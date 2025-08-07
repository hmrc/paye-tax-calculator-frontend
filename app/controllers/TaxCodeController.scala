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
import forms.UserTaxCodeFormProvider

import javax.inject.{Inject, Singleton}
import models.{QuickCalcAggregateInput, UserTaxCode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, DefaultTaxCodeProvider, SalaryRequired}
import views.html.pages.TaxCodeView

import scala.concurrent.ExecutionContext

@Singleton
class TaxCodeController @Inject() (
  override val messagesApi: MessagesApi,
  cache: QuickCalcCache,
  val controllerComponents: MessagesControllerComponents,
  navigator: Navigator,
  taxCodeView: TaxCodeView,
  userTaxCodeFormProvider: UserTaxCodeFormProvider,
  defaultTaxCodeProvider: DefaultTaxCodeProvider
)(implicit val appConfig: AppConfig, val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId
    with SalaryRequired {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[UserTaxCode] = userTaxCodeFormProvider()

  private def salaryOverHundredThousand(aggregateInput: QuickCalcAggregateInput): Boolean =
    aggregateInput.savedSalary.exists(_.amount > 100000)

  def showTaxCodeForm: Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        agg =>
          val filledForm = agg.savedTaxCode
            .map { s =>
              form.fill(s)
            }
            .getOrElse(form)
          Ok(
            taxCodeView(filledForm, agg.youHaveToldUsItems(), defaultTaxCodeProvider.defaultUkTaxCode, salaryOverHundredThousand(agg))
          )
    )

  def submitTaxCodeForm(): Action[AnyContent] =
    validateAcceptWithSessionId().async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  taxCodeView(formWithErrors,
                              aggregate.youHaveToldUsItems(),
                              defaultTaxCodeProvider.defaultUkTaxCode,
                              salaryOverHundredThousand(aggregate)
                             )
                )
              case None =>
                BadRequest(
                  taxCodeView(formWithErrors, Nil, defaultTaxCodeProvider.defaultUkTaxCode, salaryCheck = false)
                )
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
                      newTaxCode.taxCode.flatMap(Some(_)).orElse(None)
                    )
                  )
                )
              )
            updatedAggregate.flatMap { updatedAgg =>
              cache
                .save(updatedAgg)
                .map(_ =>
                  if (newTaxCode.taxCode.isEmpty) {
                    Redirect(routes.YouHaveToldUsNewController.summary())
                  } else {
                    Redirect(
                      navigator
                        .nextPageOrSummaryIfAllQuestionsAnswered(updatedAgg) {
                          routes.YouHaveToldUsNewController.summary()
                        }()
                    )
                  }
                )
            }
          }
        )
    }

}
