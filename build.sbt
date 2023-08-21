// ---- Versioning --------------------------------------------------------------

val vScala = "3.3.0"
val vMongo4cats = "0.6.15"
val vZio = "2.0.15"
val vZioCats = "23.0.03"
val vZioConfig = "4.0.0-RC16"
val vZioJson = "0.6.0"
val vZioLogging = "2.1.14"
val vZioPrelude = "1.0.0-RC20"
val vZioReactStreams = "2.0.2"

// ---- Dependencies ------------------------------------------------------------

def dependencies = Def.setting(
  Seq[ModuleID](
    "io.github.kirill5k" %% "mongo4cats-core" % vMongo4cats,
    "io.github.kirill5k" %% "mongo4cats-zio" % vMongo4cats,
    "io.github.kirill5k" %% "mongo4cats-zio-json" % vMongo4cats,
    "dev.zio" %% "zio" % vZio,
    "dev.zio" %% "zio-interop-cats" % vZioCats,
    "dev.zio" %% "zio-interop-reactivestreams" % vZioReactStreams,
    "dev.zio" %% "zio-config" % vZioConfig,
    "dev.zio" %% "zio-config-typesafe" % vZioConfig,
    "dev.zio" %% "zio-logging" % vZioLogging,
    "dev.zio" %% "zio-logging-slf4j" % vZioLogging,
    "dev.zio" %% "zio-json" % vZioJson,
    "dev.zio" %% "zio-prelude" % vZioPrelude,
    "dev.zio" %% s"zio-test" % vZio % Test,
    "dev.zio" %% s"zio-test-sbt" % vZio % Test,
    "io.github.kirill5k" %% "mongo4cats-zio-embedded" % vMongo4cats % Test
  )
)

lazy val testFramework = new TestFramework("zio.test.sbt.ZTestFramework")

// ---- Settings ----------------------------------------------------------------

lazy val LICENSE_URL = new URL(
  "https://www.apache.org/licenses/LICENSE-2.0.txt"
)
lazy val APACHE_REPO_URL =
  "https://repository.apache.org/content/groups/public/"

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalaVersion := vScala
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.rednova"
ThisBuild / organizationName := "Rednova"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

lazy val root = project
  .in(file("."))
  .settings(
    name := "mongo",
    startYear := Some(2021),
    licenses += ("Apache-2.0", LICENSE_URL),
    resolvers += "Apache public" at APACHE_REPO_URL,
    scalafmtOnCompile := true,
    Compile / scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-Xfatal-warnings",
      "-deprecation",
      "-feature",
      "-explain",
      "-indent",
      "-rewrite",
      "-Yretain-trees",
      "-Wvalue-discard"
    ),
    Compile / doc / scalacOptions ++= Seq("-no-link-warnings"),
    libraryDependencies ++= dependencies.value,
    testFrameworks := Seq(testFramework),
    Test / parallelExecution := false,
    Test / fork := true
  )
