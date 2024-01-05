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

package config

import config.features.{Feature, FeatureConfigKey, Features}

import javax.inject.Inject
import play.api.Configuration

class AppConfig @Inject() (config: Configuration) {

  lazy val host:    String = config.get[String]("host")
  lazy val appName: String = config.get[String]("appName")
  lazy val features = new Features()(config)

  lazy val betaFeedbackUrl: String =
    s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  lazy val reportAProblemPartialUrl: String =
    s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"

  lazy val reportAProblemNonJSUrl: String =
    s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val cacheUrl: String = config.get[Service]("microservice.services.cachable.session-cache").baseUrl

  lazy val domain: String = config
    .getOptional[String]("microservice.services.cachable.session-cache.domain")
    .getOrElse(throw new Exception(s"Could not find config 'services.cachable.session-cache.domain'"))
  private val contactHost:                  String = loadConfig(s"contact-frontend.host")
  private val contactFormServiceIdentifier: String = "PayeTaxCalculator"
  lazy val checkStatePensionAge:   String = config.get[String]("urls.checkStatePensionAge")
  private def loadConfig(key: String): String =
    config.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val cookies:                String = host + config.get[String]("urls.footer.cookies")
  lazy val privacy:                String = host + config.get[String]("urls.footer.privacy")
  lazy val termsConditions:        String = host + config.get[String]("urls.footer.termsConditions")
  lazy val govukHelp:              String = config.get[String]("urls.footer.govukHelp")
  lazy val accessibilityStatement: String = config.get[String]("urls.footer.accessibilityStatement")
  lazy val languageTranslationEnabled: Boolean = config.get[Boolean]("features.welsh-translation")
  lazy val timeout:   Int = config.get[Int]("timeout.timeout")
  lazy val countdown: Int = config.get[Int]("timeout.countdown")

  lazy val dateOverride: Option[String] = config.getOptional[String]("dateOverride")

  lazy val mongoTtl : Int = config.get[Int]("mongodb.timeToLiveInSeconds")
  def feedbackUrl(signedInUser: Boolean) =
    if (signedInUser) {
      s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
    } else {
      s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
    }

}
