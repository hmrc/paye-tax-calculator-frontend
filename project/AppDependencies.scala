import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlay25Version = "4.9.0"
  private val playPartialsVersion = "6.5.0"
  private val playAuthorisedFrontendVersion = "7.1.0"
  private val playConfigVersion = "7.3.0"
  private val logbackJsonLoggerVersion = "4.4.0"
  private val govukTemplateVersion = "5.26.0-play-25"
  private val playHealthVersion = "3.12.0-play-25"
  private val playUiVersion = "7.33.0-play-25"
  private val playFilters = "5.18.0"
  private val payeEstimatorVersion = "2.16.0-play-25"
  private val httpCachingClientVersion = "8.1.0"
  private val playConditionalFormMappingVersion = "0.2.0"
  private val urlBuilderVersion = "2.1.0"
  private val taxYearVersion = "0.5.0"
  private val catsCoreVersion = "1.2.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % bootstrapPlay25Version,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthorisedFrontendVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-filters" % playFilters,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "paye-estimator_sjs0.6" % payeEstimatorVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "url-builder" % urlBuilderVersion,
    "uk.gov.hmrc" %% "tax-year" % taxYearVersion,
    "org.typelevel" %% "cats-core" % catsCoreVersion
  )

  private val hmrcTestVersion = "3.6.0-play-25"
  private val scalaTestVersion = "3.0.5"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.11.3"
  private val playTestVersion = PlayVersion.current
  private val scalaTestPlusPlayVersion = "2.0.1"
  private val scalacheckVersion = "1.14.0"
  private val scalamockScalaTestSupportVersion = "3.6.0"

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "org.jsoup" % "jsoup" % jsoupVersion % scope,
    "com.typesafe.play" %% "play-test" % playTestVersion,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope,
    "org.scalamock" %% "scalamock-scalatest-support" % scalamockScalaTestSupportVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test()

}