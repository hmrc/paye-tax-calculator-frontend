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

package uk.gov.hmrc.payetaxcalculatorfrontend.config

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfig @Inject() (
  config:         Configuration,
  servicesConfig: ServicesConfig) {

  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost:  String = loadConfig(s"google-analytics.host")
  lazy val betaFeedbackUrl: String =
    s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
  lazy val reportAProblemPartialUrl: String =
    s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String =
    s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val cacheUrl: String = servicesConfig.baseUrl("cachable.session-cache")
  lazy val domain: String = config
    .getOptional[String]("microservice.services.cachable.session-cache.domain")
    .getOrElse(throw new Exception(s"Could not find config 'services.cachable.session-cache.domain'"))
  private val contactHost:                  String = loadConfig(s"contact-frontend.host")
  private val contactFormServiceIdentifier: String = "PayeTaxCalculator"

  private def loadConfig(key: String): String =
    config.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

}
