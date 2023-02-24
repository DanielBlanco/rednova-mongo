import Dependencies._

Global / onChangedBuildSource      := ReloadOnSourceChanges
ThisBuild / scalaVersion           := "3.2.2"
ThisBuild / version                := "0.1.0-SNAPSHOT"
ThisBuild / organization           := "dev.rednova"
ThisBuild / organizationName       := "Rednova"
ThisBuild / versionScheme          := Some("early-semver")
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

lazy val noPublish       = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false,
  publish / skip  := true
)

lazy val publishSettings = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := true,
  publish / skip  := false
)

lazy val commonSettings  = Seq(
  organizationName               := "MongoDB client wrapper for ZIO",
  startYear                      := Some(2021),
  licenses                       += ("Apache-2.0", new URL(
    "https://www.apache.org/licenses/LICENSE-2.0.txt"
  )),
  resolvers                      += "Apache public" at "https://repository.apache.org/content/groups/public/",
  scalafmtOnCompile              := true,
  Compile / scalacOptions       ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-explain",
    "-encoding",
    "UTF-8",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:higherKinds"
  ),
  Compile / doc / scalacOptions ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc links
  ),
  testFrameworks                 := Seq(Testing.framework)
)

lazy val root            = project
  .in(file("."))
  .settings(noPublish)
  .aggregate(`mongo-core`)
  .settings(name := "mongo")

lazy val `mongo-core`    = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name                     := "mongo-core",
    libraryDependencies     ++= Dependencies.core,
    Test / parallelExecution := false,
    Test / fork              := true
  )

