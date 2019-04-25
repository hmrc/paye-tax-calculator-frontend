/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payetaxcalculatorfrontend

import javax.inject.Inject
import play.api.Mode.Mode
import play.api.i18n.Lang
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig
import play.api.mvc.Call
import uk.gov.hmrc.payetaxcalculatorfrontend.controllers.routes

class AppConfig @Inject()(override val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {

  override protected def mode: Mode = environment.mode

  private def loadConfig(key: String): String = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost: String = loadConfig(s"contact-frontend.host")
  private val contactFormServiceIdentifier: String = "PayeTaxCalculator"

  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  lazy val betaFeedbackUrl: String = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
  lazy val reportAProblemPartialUrl: String = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val cacheUrl: String = baseUrl("cachable.session-cache")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)
}
