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
import forms.PostGraduateLoanFormProvider
import models.{PostgraduateLoanContributions, QuickCalcAggregateInput}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, SalaryRequired}
import views.html.pages.PostGraduatePlanContributionView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PostgraduateController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      MessagesControllerComponents,
  navigator:                     Navigator,
  postGraduateView:              PostGraduatePlanContributionView,
  postGraduationFromProvider:    PostGraduateLoanFormProvider
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId
    with SalaryRequired {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[PostgraduateLoanContributions] = postGraduationFromProvider()

  def showPostgraduateForm(): Action[AnyContent] =
    salaryRequired(cache, showPostgraduateContributionFormTestable)

  private[controllers] def showPostgraduateContributionFormTestable: ShowForm = { implicit request => agg =>
    val filledForm = agg.savedPostGraduateLoanContributions
      .map { s =>
        form.fill(s)
      }
      .getOrElse(form)

    Ok(postGraduateView(filledForm))
  }

  def submitPostgradLoanForm(): Action[AnyContent] =
    validateAcceptWithSessionId().async { implicit request =>
      implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            cache.fetchAndGetEntry().map {
              case Some(aggregate) =>
                BadRequest(
                  postGraduateView(formWithErrors)
                )
              case None =>
                BadRequest(
                  postGraduateView(formWithErrors)
                )
            },
          postgradLoan =>
            cache.fetchAndGetEntry().flatMap {
              case Some(aggregate) =>
                val updatedAggregate = aggregate.copy(savedPostGraduateLoanContributions = Some(postgradLoan))
                cache.save(updatedAggregate).map { _ =>
                  Redirect(
                    navigator.nextPageOrSummaryIfAllQuestionsAnswered(
                      updatedAggregate
                    ) {
                      routes.YouHaveToldUsNewController.summary
                    }()
                  )
                }
              case None =>
                cache
                  .save(
                    QuickCalcAggregateInput.newInstance
                      .copy(savedPostGraduateLoanContributions = Some(postgradLoan))
                  )
                  .map { _ =>
                    Redirect(routes.YouHaveToldUsNewController.summary)
                  }
            }
        )

    }
}
