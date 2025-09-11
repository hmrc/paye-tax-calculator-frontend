import play.core.PlayVersion
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val bootstrapPlay30Version = "10.1.0"
  private val taxYearVersion = "6.0.0"
  private val taxKalcVersion = "2.15.0"
  private val hmrcFrontend = "12.10.0"
  private val mongoVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapPlay30Version,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapPlay30Version,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % hmrcFrontend,
    "uk.gov.hmrc"       %% "tax-year"                   % taxYearVersion,
    "uk.gov.hmrc"        % "tax-kalculator-jvm"         % taxKalcVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoVersion
  )

  private val scalaTestPlusMockitoVersion = "3.2.12.0"
  private val scalacheckVersion = "1.18.1"
  private val scalamockScalaTestSupportVersion = "7.4.0"

  def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapPlay30Version           % scope,
    "org.scalatestplus" %% "mockito-4-5"             % scalaTestPlusMockitoVersion      % scope,
    "org.scalacheck"    %% "scalacheck"              % scalacheckVersion                % scope,
    "org.scalamock"     %% "scalamock"               % scalamockScalaTestSupportVersion % scope,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % mongoVersion                     % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test()

}
