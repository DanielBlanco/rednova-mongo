Global / onChangedBuildSource      := ReloadOnSourceChanges
ThisBuild / scalaVersion           := "3.2.2"
ThisBuild / version                := "0.1.0-SNAPSHOT"
ThisBuild / organization           := "dev.rednova"
ThisBuild / organizationName       := "Rednova"
ThisBuild / versionScheme          := Some("early-semver")
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

lazy val LICENSE_URL = new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")
lazy val APACHE_REPO_URL = "https://repository.apache.org/content/groups/public/"
lazy val root            = project
  .in(file("."))
  .settings(
    name                           := "mongo",
    startYear                      := Some(2021),
    licenses                       += ("Apache-2.0", LICENSE_URL),
    resolvers                      += "Apache public" at APACHE_REPO_URL,
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
    libraryDependencies           ++= Dependencies.libs,
    libraryDependencies           ++= Dependencies.Testing.libs,
    testFrameworks                := Seq(Dependencies.Testing.framework),
    Test / parallelExecution      := false,
    Test / fork                   := true
  )

