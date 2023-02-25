import sbt._

object Dependencies {
  object V {
    val mongo4cats      = "0.6.7"
    val zio             = "2.0.9"
    val zioCats         = "23.0.0.0"
    val zioConfig       = "4.0.0-RC9"
    val zioJson         = "0.4.2"
    val zioLogging      = "2.1.10"
    val zioPrelude      = "1.0.0-RC16"
    val zioReactStreams = "2.0.1"
  }

  val libs = Seq(
    "io.github.kirill5k" %% "mongo4cats-core" % V.mongo4cats,
    "io.github.kirill5k" %% "mongo4cats-zio"  % V.mongo4cats,
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

  object Testing {
    val libs = Seq(
      "dev.zio"            %% s"zio-test"               % V.zio        % Test,
      "dev.zio"            %% s"zio-test-sbt"           % V.zio        % Test,
      "io.github.kirill5k" %% "mongo4cats-zio-embedded" % V.mongo4cats % Test
    )

    val framework = new TestFramework("zio.test.sbt.ZTestFramework")
  }
}
