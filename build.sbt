import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt.{Resolver, _}
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.SbtAutoBuildPlugin

val appName: String = "paye-tax-calculator-frontend"

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*repositories.*;" +
    ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;" +
    ".*ControllerConfiguration;.*LanguageSwitchController",
    coverageMinimumStmtTotal := 74,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
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
          // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
      group(
        Seq(
          "lib/govuk-frontend/govuk/all.js",
          "javascripts/jquery.min.js",
          "javascripts/app.js",
          "javascripts/print.js",
          "javascripts/timeoutDialog.js"
        )
      )
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat, uglify),
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("application.js")
  )
  .settings(
    scalaVersion := "2.12.15",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
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
