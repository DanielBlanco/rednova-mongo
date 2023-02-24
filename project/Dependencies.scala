import sbt._

object Dependencies {
  object V {
    val mongo4cats      = "0.6.6"
    val circe           = "0.14.3"
    val zio             = "2.0.5"
    val zioCats         = "3.3.0"
    val zioConfig       = "3.0.7"
    val zioJson         = "0.4.2"
    val zioLogging      = "2.1.7"
    val zioMagic        = "0.3.11"
    val zioPrelude      = "1.0.0-RC16"
    val zioReactStreams = "2.0.0"
  }

  /** Used in RepoTests */
  object Circe {
    val libs = Seq(
      "io.circe"           %% "circe-core"       % V.circe,
      "io.circe"           %% "circe-generic"    % V.circe,
      "io.circe"           %% "circe-parser"     % V.circe,
      "io.github.kirill5k" %% "mongo4cats-circe" % V.mongo4cats
    )
  }

  object MongoDB {
    val libs = Seq(
      "io.github.kirill5k" %% "mongo4cats-core" % V.mongo4cats,
      "io.github.kirill5k" %% "mongo4cats-core" % V.mongo4cats,
      "io.github.kirill5k" %% "mongo4cats-zio"  % V.mongo4cats
    )
  }

  object Testing {
    val libs = Seq(
      "dev.zio"            %% s"zio-test"               % V.zio        % Test,
      "dev.zio"            %% s"zio-test-sbt"           % V.zio        % Test,
      "io.github.kirill5k" %% "mongo4cats-zio-embedded" % V.mongo4cats % Test
    )

    val framework = new TestFramework("zio.test.sbt.ZTestFramework")
  }

  object ZIO {
    val libs = Seq(
      "dev.zio" %% "zio"                         % V.zio,
      "dev.zio" %% "zio-interop-cats"            % V.zioCats,
      "dev.zio" %% "zio-interop-reactivestreams" % V.zioReactStreams,
      "dev.zio" %% "zio-config"                  % V.zioConfig,
      "dev.zio" %% "zio-config-typesafe"         % V.zioConfig,
      "dev.zio" %% "zio-logging"                 % V.zioLogging,
      "dev.zio" %% "zio-logging-slf4j"           % V.zioLogging,
      "dev.zio" %% "zio-json"                    % V.zioJson,
      "dev.zio" %% "zio-prelude"                 % V.zioPrelude
    )
  }

  def core =
    MongoDB.libs ++ Testing.libs ++ ZIO.libs

  def circe =
    Circe.libs ++ Testing.libs

  def repo = core
}
