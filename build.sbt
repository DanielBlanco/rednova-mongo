Global / onChangedBuildSource      := ReloadOnSourceChanges
ThisBuild / scalaVersion           := "3.2.2"
ThisBuild / version                := "0.1.0-SNAPSHOT"
ThisBuild / organization           := "dev.rednova"
ThisBuild / organizationName       := "Rednova"
ThisBuild / versionScheme          := Some("early-semver")
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

lazy val root            = project
  .in(file("."))
  .settings(
    startYear                      := Some(2021),
    licenses                       += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    resolvers                      += "Apache public" at "https://repository.apache.org/content/groups/public/",
    scalafmtOnCompile              := true,
    Compile / scalacOptions       ++= Seq(
      "-encoding",
      "UTF-8",
      "-Xfatal-warnings",
      "-deprecation",
      "-feature",
      "-explain",
      "-indent",
      "-rewrite",
    ),
    Compile / doc / scalacOptions ++= Seq("-no-link-warnings"),
    libraryDependencies     ++= Dependencies.libs,
    testFrameworks                 := Seq(Dependencies.Testing.framework)
    Test / parallelExecution := false,
    Test / fork              := true
  )

