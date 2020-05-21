/*
 * Copyright 2020 HM Revenue & Customs
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
import models.Salary._
import forms._
import javax.inject.{Inject, Singleton}
import models.{PayPeriodDetail, QuickCalcAggregateInput, Salary}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{ControllerComponents, _}
import services.{Navigator, QuickCalcCache}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.controller.BackendBaseController
import utils.ActionWithSessionId
import views.html.pages._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class QuickCalcController @Inject() (
  override val messagesApi:      MessagesApi,
  cache:                         QuickCalcCache,
  val controllerComponents:      ControllerComponents,
  navigator:                     Navigator
)(implicit val appConfig:        AppConfig,
  implicit val executionContext: ExecutionContext)
    extends BackendBaseController
    with I18nSupport
    with ActionWithSessionId {

  implicit val parser: BodyParser[AnyContent] = parse.anyContent

  private[controllers] val showTacCodeFormTestable: ShowForm =
    implicit request =>
      aggregate => {
        val form = aggregate.savedTaxCode.map(UserTaxCode.form.fill).getOrElse(UserTaxCode.form)
        Ok(tax_code(form, aggregate.youHaveToldUsItems))
      }

  def redirectToSalaryForm(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    Future.successful(Redirect(routes.SalaryController.showSalaryForm()))
  }

  def summary(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        aggregate =>
          if (aggregate.allQuestionsAnswered)
            Ok(you_have_told_us(aggregate.youHaveToldUsItems))
          else
            redirectToNotYetDonePage(aggregate)
    )

  def showResult(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        aggregate =>
          if (aggregate.allQuestionsAnswered) {
            Ok(result(TaxResult.taxCalculation(aggregate), UserTaxCode.startOfCurrentTaxYear))
          } else redirectToNotYetDonePage(aggregate)
    )

  private def redirectToNotYetDonePage(aggregate: QuickCalcAggregateInput): Result =
    if (aggregate.savedTaxCode.isEmpty)
      Redirect(routes.QuickCalcController.showTaxCodeForm())
    else if (aggregate.savedSalary.isEmpty)
      Redirect(routes.SalaryController.showSalaryForm())
    else
      Redirect(routes.QuickCalcController.showStatePensionForm())

  def showStatePensionForm(): Action[AnyContent] = salaryRequired(cache, showStatePensionFormTestable)

  private[controllers] def showStatePensionFormTestable: ShowForm = { implicit request => agg =>
    val form = agg.savedIsOverStatePensionAge
      .map(OverStatePensionAge.form.fill)
      .getOrElse(OverStatePensionAge.form)
    Ok(state_pension(form, agg.youHaveToldUsItems))
  }

  def submitStatePensionForm(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    OverStatePensionAge.form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          cache.fetchAndGetEntry().map {
            case Some(aggregate) =>
              BadRequest(state_pension(formWithErrors, aggregate.youHaveToldUsItems))
            case None =>
              BadRequest(state_pension(formWithErrors, Nil))
          },
        userAge =>
          cache.fetchAndGetEntry().flatMap {
            case Some(aggregate) =>
              val updatedAggregate = aggregate.copy(savedIsOverStatePensionAge = Some(userAge))
              cache.save(updatedAggregate).map { _ =>
                Redirect(navigator.nextPageOrSummaryIfAllQuestionsAnswered(updatedAggregate) {
                  routes.QuickCalcController.showTaxCodeForm()
                })
              }
            case None =>
              cache.save(QuickCalcAggregateInput.newInstance.copy(savedIsOverStatePensionAge = Some(userAge))).map {
                _ => Redirect(routes.QuickCalcController.showTaxCodeForm())
              }
          }
      )
  }

  def showTaxCodeForm(): Action[AnyContent] = salaryRequired(cache, showTacCodeFormTestable)

  def submitTaxCodeForm(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    UserTaxCode.form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          cache.fetchAndGetEntry().map {
            case Some(aggregate) =>
              BadRequest(tax_code(formWithErrors, aggregate.youHaveToldUsItems))
            case None => BadRequest(tax_code(formWithErrors, Nil))
          },
        newTaxCode => {
          val updatedAggregate = cache
            .fetchAndGetEntry()
            .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
            .map(agg =>
              if (newTaxCode.gaveUsTaxCode) agg.copy(savedTaxCode = Some(newTaxCode), savedScottishRate = None)
              else
                agg.copy(savedTaxCode = Some(UserTaxCode(gaveUsTaxCode = false, Some(UserTaxCode.defaultUkTaxCode))))
            )

          updatedAggregate.flatMap(agg =>
            cache
              .save(agg)
              .map(_ =>
                if (newTaxCode.gaveUsTaxCode) {
                  Redirect(navigator.nextPageOrSummaryIfAllQuestionsAnswered(agg) {
                    routes.QuickCalcController.summary()
                  })
                } else Redirect(routes.QuickCalcController.showScottishRateForm())
              )
          )
        }
      )
  }

  def showScottishRateForm(): Action[AnyContent] =
    salaryRequired(
      cache,
      implicit request =>
        aggregate => {
          implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

          val form = aggregate.savedScottishRate.map(ScottishRate.form.fill).getOrElse(ScottishRate.form)
          Ok(scottish_income_tax_rate(form, aggregate.youHaveToldUsItems))
        }
    )

  def submitScottishRateForm(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    ScottishRate.form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          cache
            .fetchAndGetEntry()
            .map {
              case Some(aggregate) => aggregate.youHaveToldUsItems
              case None            => Nil
            }
            .map(itemList => BadRequest(scottish_income_tax_rate(formWithErrors, itemList))),
        scottish => {
          val taxCode =
            if (scottish.value)
              UserTaxCode.defaultScottishTaxCode
            else
              UserTaxCode.defaultUkTaxCode

          val updatedAggregate = cache
            .fetchAndGetEntry()
            .map(_.getOrElse(QuickCalcAggregateInput.newInstance))
            .map(
              _.copy(
                savedTaxCode      = Some(UserTaxCode(gaveUsTaxCode = false, Some(taxCode))),
                savedScottishRate = Some(ScottishRate(scottish.value))
              )
            )
          updatedAggregate
            .map(cache.save)
            .map(_ => Redirect(routes.QuickCalcController.summary()))
        }
      )
  }

  def restartQuickCalc(): Action[AnyContent] = validateAcceptWithSessionId.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    cache.fetchAndGetEntry().flatMap {
      case Some(aggregate) =>
        val updatedAggregate = aggregate.copy(None, None, None, None, None)
        cache.save(updatedAggregate).map(_ => Redirect(routes.SalaryController.showSalaryForm()))
      case None =>
        Future.successful(Redirect(routes.SalaryController.showSalaryForm()))
    }
  }

  private def salaryRequired[T](
    cache:         QuickCalcCache,
    furtherAction: Request[AnyContent] => QuickCalcAggregateInput => Result
  ): Action[AnyContent] =
    validateAcceptWithSessionId.async { implicit request =>
      implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

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
