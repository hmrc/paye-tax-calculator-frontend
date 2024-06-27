import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlay30Version            = "8.6.0"
  private val taxYearVersion                    = "4.0.0"
  private val taxKalcVersion                    = "2.12.1"
  private val hmrcFrontend                      = "9.10.0"
  private val mongoVersion                      = "1.9.0"


  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-30"    % bootstrapPlay30Version,
    "uk.gov.hmrc"          %% "bootstrap-backend-play-30"      % bootstrapPlay30Version,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30"    % hmrcFrontend,
    "uk.gov.hmrc"          %% "tax-year"                      % taxYearVersion,
    "uk.gov.hmrc"          % "tax-kalculator-jvm"             % taxKalcVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-30"            % mongoVersion,
  )

  private val jsoupVersion                     = "1.17.2"
  private val scalaTestPlusMockitoVersion      = "3.2.10.0"
  private val scalacheckVersion                = "1.18.0"
  private val scalamockScalaTestSupportVersion = "6.0.0"
  private val flexmarkVersion                   = "0.64.8"

  def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"      % bootstrapPlay30Version           % scope,
    "org.scalatestplus"    %% "mockito-3-12"                % scalaTestPlusMockitoVersion      % scope,
    "org.scalacheck"       %% "scalacheck"                  % scalacheckVersion                % scope,
    "org.scalamock"        %% "scalamock"                   % scalamockScalaTestSupportVersion % scope,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30"    % mongoVersion                      % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test()

}
