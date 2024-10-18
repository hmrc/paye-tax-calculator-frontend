import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt.{Resolver, _}
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName: String = "paye-tax-calculator-frontend"

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*config.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController",
    coverageMinimumStmtTotal := 79,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(PlayKeys.playDefaultPort := 7788)
  .settings(scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(majorVersion := 0)
  .settings(defaultSettings(): _*)
  .settings(
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    RoutesKeys.routesImport ++= Seq(
      "models._"
    ),

    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.calculator.model._",
      "views.ViewUtils._",
      "controllers.routes._",
      "config.AppConfig"
    ),
    resolvers += Resolver.jcenterRepo,
  )
  .settings(
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies(),
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false,
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
