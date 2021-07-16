import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "5.7.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "5.7.0",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.80.0-play-27",
    "uk.gov.hmrc" %% "simple-reactivemongo" % "8.0.0-play-27",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.83.0-play-27"
  )

  def commonTestDependencies(scope: Configuration): Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
    "org.scalatest" %% "scalatest" % "3.2.8" % scope,
    "org.jsoup" % "jsoup" % "1.13.1" % scope,
    "com.typesafe.play" %% "play-test" % current % scope,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
  )

  val test: Seq[ModuleID] = Seq(
    // Bootstrap import needed to access stubMessagesControllerComponents in unit tests
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "5.7.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "5.7.0",
    "org.mockito" % "mockito-core" % "3.11.2" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.3.0" % Test
  ) ++ commonTestDependencies(Test)

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.29.0" % IntegrationTest
  ) ++ commonTestDependencies(IntegrationTest)

  def apply(): Seq[ModuleID] = compile ++ test ++ it

}
