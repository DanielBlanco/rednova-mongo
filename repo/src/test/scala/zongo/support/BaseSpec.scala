package dev.rednova.mongo.support

import dev.rednova.mongo.*
import zio.*
import zio.config.*
import zio.config.syntax.*
import zio.config.ConfigDescriptor.*
import zio.config.typesafe.*
import zio.test.*
import zio.test.Assertion.*

trait BaseSpec extends ZIOSpecDefault:
  override def aspects = Chunk(TestAspect.timeout(60.seconds))

  final val TIMEOUT = 1.second
  final val TEST_DB = "rednova_mongo_test"

  // ---- ZIO Layer setup

  lazy val mongoLayer: ZLayer[SpecConfig, Nothing, Mongo]       =
    for
      env   <- ZLayer.service[SpecConfig]
      mongo <- Mongo.live(env.get.mongo.uri)
    yield mongo

  lazy val specLayer: ZLayer[TestEnvironment, Nothing, SpecEnv] =
    ZLayer
      .makeSome[TestEnvironment, SpecEnv](
        SpecConfig.live,
        mongoLayer,
        ItemsRepo.layer(TEST_DB)
      )
      .orDie

// ---- Helper functions

// protected def clearDB = Mongo.clearDatabase.orDie
