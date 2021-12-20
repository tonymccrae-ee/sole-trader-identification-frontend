import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "sole-trader-identification-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, SbtArtifactory)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies()
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort := 9717)

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._"
)

Keys.fork in Test := true
javaOptions in Test += "-Dlogger.resource=logback-test.xml"
parallelExecution in Test := true

Keys.fork in IntegrationTest := true
unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "it")).value
javaOptions in IntegrationTest += "-Dlogger.resource=logback-test.xml"
addTestReportOption(IntegrationTest, "int-test-reports")
parallelExecution in IntegrationTest := false
majorVersion := 1

scalaVersion :=  "2.12.12"
