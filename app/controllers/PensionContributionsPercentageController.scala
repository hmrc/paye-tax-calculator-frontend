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
import forms.PensionContributionFormProvider
import models.{PensionContributions, QuickCalcAggregateInput}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ActionWithSessionId, SalaryRequired}
import views.html.pages.PensionContributionsPercentageView
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PensionContributionsPercentageController @Inject()(
  override val messagesApi:           MessagesApi,
  cache:                              QuickCalcCache,
  val controllerComponents:           MessagesControllerComponents,
  navigator:                          Navigator,
  pensionContributionsPercentageView: PensionContributionsPercentageView,
  pensionsContributionFormProvider:   PensionContributionFormProvider
)(implicit val appConfig:             AppConfig,
  implicit val executionContext:      ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId with SalaryRequired{

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[PensionContributions] = pensionsContributionFormProvider()

  def showPensionContributionForm(): Action[AnyContent] = {
    salaryRequired(
      cache, { implicit fromRequestAndSession => agg =>
        val filledForm = agg.savedPensionContributions.map(_.gaveUsPercentageAmount) match {
          case Some(false) => form
          case _ => {
            agg.savedPensionContributions
              .map { s =>
                form.fill(s)
              }.getOrElse(form)
          }
        }
        Ok(
          pensionContributionsPercentageView(filledForm,
            agg.additionalQuestionItems())
        )
      }
    )
  }

  def submitPensionContribution(): Action[AnyContent] =
    validateAcceptWithSessionId().async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  pensionContributionsPercentageView(formWithErrors, aggregate.additionalQuestionItems())
                )
              case None =>
                BadRequest(
                  pensionContributionsPercentageView(formWithErrors, Nil)
                )
            },
          (newPensionContributions: PensionContributions) => {
            val updateAggregate = cache.fetchAndGetEntry().map(_.getOrElse(QuickCalcAggregateInput.newInstance))
              .map(agg =>
                agg.copy(
                  savedPensionContributions = Some(
                    newPensionContributions.copy(
                      gaveUsPercentageAmount = true
                    ),
                  )
                )
              )

            updateAggregate.flatMap { updatedAgg =>
              cache.save(updatedAgg).map(_ =>
                Redirect(navigator.nextPageOrSummaryIfAllQuestionsAnswered(updatedAgg) {
                  routes.YouHaveToldUsNewController.summary
                }())
              )
            }
          }
        )

    }
}
