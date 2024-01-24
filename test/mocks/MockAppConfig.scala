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

package mocks

import config.AppConfig
import config.features.Features
import play.api.Configuration

class MockAppConfig(val runModeConfiguration: Configuration) extends AppConfig {
  override val host: String = "host"
  override val appName: String = "appName"
  override val features: Features = new Features()(runModeConfiguration)
  override val betaFeedbackUrl: String = "contactHost/contact/beta-feedback-unauthenticated?service=PayeTaxCalculator"
  override val reportAProblemPartialUrl: String = "/contact/problem_reports_ajax?service=PayeTaxCalculator"
  override val reportAProblemNonJSUrl: String = "/contactHost/contact/problem_reports_nonjs?service=PayeTaxCalculator"
  override val checkStatePensionAge: String = "https://www.gov.uk/state-pension-age"
  override val cookies: String = "/help/cookies"
  override val privacy: String = "/help/privacy"
  override val termsConditions: String = "/help/terms-and-conditions"
  override val govukHelp: String = "https://www.gov.uk/help"
  override val accessibilityStatement: String = "/accessibility-statement/take-home-pay-calculator"
  override val languageTranslationEnabled: Boolean = false
  override val timeout: Int = 900
  override val countdown: Int = 120
  override val dateOverride: Option[String] = None
  override val mongoTtl: Int = 3600

  override def feedbackUrl(signedInUser: Boolean): String = "/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
}
