import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlay28Version            = "8.2.0"
  private val playPartialsVersion               = "8.3.0-play-28"
  private val playConditionalFormMappingVersion = "1.13.0-play-28"
  private val urlBuilderVersion                 = "3.6.0-play-28"
  private val taxYearVersion                    = "4.0.0"
  private val taxKalcVersion                    = "2.10.0"
  private val catsCoreVersion                   = "2.3.0"
  private val hmrcFrontend                      = "8.5.0"
  private val mongoVersion                      = "1.3.0"


  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28"    % bootstrapPlay28Version,
    "uk.gov.hmrc"          %% "bootstrap-backend-play-28"     % bootstrapPlay28Version,
    "uk.gov.hmrc"          %% "play-partials"                 % playPartialsVersion,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-28"    % hmrcFrontend,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc"          %% "tax-year"                      % taxYearVersion,
    "uk.gov.hmrc"          % "tax-kalculator-jvm"             % taxKalcVersion,
    "org.typelevel"        %% "cats-core"                     % catsCoreVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-28"            % mongoVersion,
  )

  private val pegdownVersion                   = "1.6.0"
  private val jsoupVersion                     = "1.11.3"
  private val scalaTestPlusMockitoVersion      = "3.2.10.0"
  private val scalacheckVersion                = "1.14.1"
  private val scalamockScalaTestSupportVersion = "6.0.0-M2"
  private val mockitoVersion                   = "4.1.0"
  private val flexmarkVersion                   = "0.62.2"

  def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-28"      % bootstrapPlay28Version           % scope,
    "org.pegdown"          % "pegdown"                      % pegdownVersion                   % scope,
    "org.jsoup"            % "jsoup"                        % jsoupVersion                     % scope,
    "org.scalatestplus"    %% "mockito-3-12"                % scalaTestPlusMockitoVersion      % scope,
    "org.scalacheck"       %% "scalacheck"                  % scalacheckVersion                % scope,
    "org.mockito"          % "mockito-core"                 % mockitoVersion                   % scope,
    "org.scalamock"        %% "scalamock"                   % scalamockScalaTestSupportVersion % scope,
    "com.vladsch.flexmark"  % "flexmark-all"                   % flexmarkVersion                  % scope,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-28"    % mongoVersion                      % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test()

}
