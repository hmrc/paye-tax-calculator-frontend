import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlay26Version            = "1.7.0"
  private val playPartialsVersion               = "6.11.0-play-26"
  private val logbackJsonLoggerVersion          = "4.8.0"
  private val govukTemplateVersion              = "5.55.0-play-26"
  private val playHealthVersion                 = "3.15.0-play-26"
  private val playUiVersion                     = "8.9.0-play-26"
  private val httpCachingClientVersion          = "9.0.0-play-26"
  private val playConditionalFormMappingVersion = "1.2.0-play-26"
  private val urlBuilderVersion                 = "3.3.0-play-26"
  private val taxYearVersion                    = "1.1.0"
  private val taxKalcVersion                    = "0.9.0"
  private val catsCoreVersion                   = "2.1.0"
  private val govUkTemplate                   = "0.44.0-play-26"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"   %% "bootstrap-play-26"             % bootstrapPlay26Version,
    "uk.gov.hmrc"   %% "play-partials"                 % playPartialsVersion,
    "uk.gov.hmrc"   %% "logback-json-logger"           % logbackJsonLoggerVersion,
    "uk.gov.hmrc"   %% "govuk-template"                % govukTemplateVersion,
    "uk.gov.hmrc"   %% "play-health"                   % playHealthVersion,
    "uk.gov.hmrc"   %% "play-ui"                       % playUiVersion,
    "uk.gov.hmrc"   %% "http-caching-client"           % httpCachingClientVersion,
    "uk.gov.hmrc"   %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc"   %% "url-builder"                   % urlBuilderVersion,
    "uk.gov.hmrc"   %% "tax-year"                      % taxYearVersion,
    "uk.gov.hmrc"   % "tax-kalculator-jvm"             % taxKalcVersion,
    "org.typelevel" %% "cats-core"                     % catsCoreVersion,
    "uk.gov.hmrc" %% "play-frontend-govuk" % govUkTemplate
  )

  private val hmrcTestVersion                  = "3.9.0-play-26"
  private val scalaTestVersion                 = "3.0.8"
  private val pegdownVersion                   = "1.6.0"
  private val jsoupVersion                     = "1.11.3"
  private val playTestVersion                  = PlayVersion.current
  private val scalaTestPlusPlayVersion         = "3.1.2"
  private val scalacheckVersion                = "1.14.1"
  private val scalamockScalaTestSupportVersion = "3.6.0"

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "hmrctest"                    % hmrcTestVersion % scope,
    "org.scalatest"          %% "scalatest"                   % scalaTestVersion % scope,
    "org.pegdown"            % "pegdown"                      % pegdownVersion % scope,
    "org.jsoup"              % "jsoup"                        % jsoupVersion % scope,
    "com.typesafe.play"      %% "play-test"                   % playTestVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"          % scalaTestPlusPlayVersion % scope,
    "org.scalacheck"         %% "scalacheck"                  % scalacheckVersion % scope,
    "org.mockito"                 %  "mockito-all"          % "1.10.19" % scope,
    "org.scalamock"          %% "scalamock-scalatest-support" % scalamockScalaTestSupportVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test()

}
