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
import forms.StudentLoansFormProvider
import models.{QuickCalcAggregateInput, StudentLoanContributions}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession
import utils.{ActionWithSessionId, SalaryRequired}
import views.html.pages.StudentLoansContributionView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StudentLoanContributionsController @Inject() (
  override val messagesApi: MessagesApi,
  cache: QuickCalcCache,
  val controllerComponents: MessagesControllerComponents,
  navigator: Navigator,
  studentLoansView: StudentLoansContributionView,
  studentLoansFormProvider: StudentLoansFormProvider
)(implicit val appConfig: AppConfig, val executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ActionWithSessionId
    with SalaryRequired {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  val form: Form[StudentLoanContributions] = studentLoansFormProvider()

  def showStudentLoansForm(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        agg => {
          val filledForm = agg.savedStudentLoanContributions
            .map { s =>
              form.fill(s)
            }
            .getOrElse(form)
          Ok(studentLoansView(filledForm))
        }
    )

  def submitStudentLoansContribution(): Action[AnyContent] = validateAcceptWithSessionId().async { implicit request =>
    implicit val hc: HeaderCarrier = fromRequestAndSession(request, request.session)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(studentLoansView(formWithErrors))),
        (newStudentLoanContribution: StudentLoanContributions) => {
          val updatedAggregate = cache
            .fetchAndGetEntry()
            .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
            .map(agg =>
              agg.copy(
                savedStudentLoanContributions = if (newStudentLoanContribution.studentLoanPlan.isDefined) {
                  Some(
                    StudentLoanContributions(newStudentLoanContribution.studentLoanPlan)
                  )
                } else {
                  None
                }
              )
            )
          updatedAggregate.flatMap(updatedAgg =>
            cache
              .save(updatedAgg)
              .map(_ =>
                Redirect(navigator.nextPageOrSummaryIfAllQuestionsAnswered(updatedAgg) {
                  routes.YouHaveToldUsNewController.summary()
                }())
              )
          )
        }
      )

  }

}
