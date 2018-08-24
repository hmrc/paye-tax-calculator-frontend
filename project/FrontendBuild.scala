import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "paye-tax-calculator-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "10.1.0",
    "uk.gov.hmrc" %% "play-partials" % "6.1.0",
    "uk.gov.hmrc" %% "play-authorised-frontend" % "7.0.0",
    "uk.gov.hmrc" %% "play-config" % "4.3.3",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.22.0",
    "uk.gov.hmrc" %% "play-health" % "2.2.0",
    "uk.gov.hmrc" %% "play-ui" % "7.21.0",
    "uk.gov.hmrc" %% "paye-estimator_sjs0.6" % "2.0.0",
    "uk.gov.hmrc" %% "http-caching-client" % "7.1.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "0.2.0",
    "uk.gov.hmrc" %% "url-builder" % "2.1.0",
    "uk.gov.hmrc" %% "tax-year" % "0.4.0",
    "org.typelevel" %% "cats-core" % "1.2.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.5" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.11.3" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
    "org.scalacheck" %% "scalacheck" % "1.14.0" % scope,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope
  )

}
