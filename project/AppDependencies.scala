import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlay28Version            = "5.16.0"
  private val playPartialsVersion               = "8.2.0-play-28"
  private val logbackJsonLoggerVersion          = "4.9.0"
  private val httpCachingClientVersion          = "9.5.0-play-28"
  private val playConditionalFormMappingVersion = "1.10.0-play-28"
  private val urlBuilderVersion                 = "3.5.0-play-28"
  private val taxYearVersion                    = "1.2.0"
  private val taxKalcVersion                    = "2.3.0"
  private val catsCoreVersion                   = "2.3.0"
  private val hmrcFrontend                      = "1.26.0-play-28"
  private val flexmarkVersion                   = "0.62.2"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28"    % bootstrapPlay28Version,
    "uk.gov.hmrc"          %% "bootstrap-backend-play-28"     % bootstrapPlay28Version,
    "uk.gov.hmrc"          %% "play-partials"                 % playPartialsVersion,
    "uk.gov.hmrc"          %% "logback-json-logger"           % logbackJsonLoggerVersion,
    "uk.gov.hmrc"          %% "play-frontend-hmrc"            % hmrcFrontend,
    "uk.gov.hmrc"          %% "http-caching-client"           % httpCachingClientVersion,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc"          %% "url-builder"                   % urlBuilderVersion,
    "uk.gov.hmrc"          %% "tax-year"                      % taxYearVersion,
    "uk.gov.hmrc"          % "tax-kalculator-jvm"             % taxKalcVersion,
    "org.typelevel"        %% "cats-core"                     % catsCoreVersion,
    "com.vladsch.flexmark" % "flexmark-all"                   % flexmarkVersion
  )

  private val pegdownVersion                   = "1.6.0"
  private val jsoupVersion                     = "1.11.3"
  private val scalaTestPlusMockitoVersion      = "3.2.10.0"
  private val scalacheckVersion                = "1.14.1"
  private val scalamockScalaTestSupportVersion = "3.6.0"
  private val mockitoVersion                   = "4.1.0"

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-28"      % bootstrapPlay28Version           % scope,
    "org.pegdown"          % "pegdown"                      % pegdownVersion                   % scope,
    "org.jsoup"            % "jsoup"                        % jsoupVersion                     % scope,
    "org.scalatestplus"    %% "mockito-3-12"                % scalaTestPlusMockitoVersion      % scope,
    "org.scalacheck"       %% "scalacheck"                  % scalacheckVersion                % scope,
    "org.mockito"          % "mockito-core"                 % mockitoVersion                   % scope,
    "org.scalamock"        %% "scalamock-scalatest-support" % scalamockScalaTestSupportVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test()

}
