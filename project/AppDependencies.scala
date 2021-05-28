import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlay27Version            = "5.1.0"
  private val playPartialsVersion               = "8.1.0-play-27"
  private val logbackJsonLoggerVersion          = "4.9.0"
  private val govukTemplateVersion              = "5.66.0-play-27"
  private val playHealthVersion                 = "3.16.0-play-27"
  private val playUiVersion                     = "9.2.0-play-27"
  private val httpCachingClientVersion          = "9.4.0-play-27"
  private val playConditionalFormMappingVersion = "1.9.0-play-27"
  private val urlBuilderVersion                 = "3.5.0-play-27"
  private val taxYearVersion                    = "1.2.0"
  private val taxKalcVersion                    = "1.2.0"
  private val catsCoreVersion                   = "2.3.0"
  private val playFrontendGovukVersion          = "0.71.0-play-27"
  private val hmrcFrontend                      = "0.62.0-play-27"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"   %% "bootstrap-frontend-play-27"    % bootstrapPlay27Version,
    "uk.gov.hmrc"   %% "bootstrap-backend-play-27"     % bootstrapPlay27Version,
    "uk.gov.hmrc"   %% "play-partials"                 % playPartialsVersion,
    "uk.gov.hmrc"   %% "logback-json-logger"           % logbackJsonLoggerVersion,
    "uk.gov.hmrc"   %% "govuk-template"                % govukTemplateVersion,
    "uk.gov.hmrc"   %% "play-frontend-hmrc"            % hmrcFrontend,
    "uk.gov.hmrc"   %% "play-health"                   % playHealthVersion,
    "uk.gov.hmrc"   %% "play-ui"                       % playUiVersion,
    "uk.gov.hmrc"   %% "http-caching-client"           % httpCachingClientVersion,
    "uk.gov.hmrc"   %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc"   %% "url-builder"                   % urlBuilderVersion,
    "uk.gov.hmrc"   %% "tax-year"                      % taxYearVersion,
    "uk.gov.hmrc"   % "tax-kalculator-jvm"             % taxKalcVersion,
    "org.typelevel" %% "cats-core"                     % catsCoreVersion,
    "uk.gov.hmrc"   %% "play-frontend-govuk"           % playFrontendGovukVersion
  )

  private val hmrcTestVersion                  = "3.9.0-play-26"
  private val scalaTestVersion                 = "3.0.8"
  private val pegdownVersion                   = "1.6.0"
  private val jsoupVersion                     = "1.11.3"
  private val playTestVersion                  = PlayVersion.current
  private val scalaTestPlusPlayVersion         = "4.0.3"
  private val scalacheckVersion                = "1.14.1"
  private val scalamockScalaTestSupportVersion = "3.6.0"
  private val mockitoAllVersion                = "1.10.19"
  private val scalatestWordspecVersion         = "3.2.7"
  private val flexmarkVersion                  = "0.35.10"

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "hmrctest"                    % hmrcTestVersion % scope,
    "org.scalatest"          %% "scalatest"                   % scalaTestVersion % scope,
    "org.pegdown"            % "pegdown"                      % pegdownVersion % scope,
    "org.jsoup"              % "jsoup"                        % jsoupVersion % scope,
    "com.typesafe.play"      %% "play-test"                   % playTestVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"          % scalaTestPlusPlayVersion % scope,
    "org.scalacheck"         %% "scalacheck"                  % scalacheckVersion % scope,
    "org.mockito"            % "mockito-all"                  % mockitoAllVersion % scope,
    "org.scalamock"          %% "scalamock-scalatest-support" % scalamockScalaTestSupportVersion % scope,
    "org.scalatest"          %% "scalatest-wordspec"          % scalatestWordspecVersion % scope,
    "com.vladsch.flexmark"   % "flexmark-all"                 % flexmarkVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test()

}
