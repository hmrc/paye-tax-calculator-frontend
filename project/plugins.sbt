resolvers += Resolver.url(
  "HMRC-open-artefacts-ivy",
  url("https://open.artefacts.tax.service.gov.uk/ivy2")
)(Resolver.ivyStylePatterns)

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.21.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.5.0")

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.2")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")

addSbtPlugin("net.ground5hark.sbt" % "sbt-concat" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "3.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "2.0.0")

addSbtPlugin("io.github.irundaia" % "sbt-sassify" % "1.5.2")