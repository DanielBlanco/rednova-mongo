package mongo4cats.zio

import mongo4cats.bson.*
import mongo4cats.bson.syntax.*
import mongo4cats.zio.*
import _root_.zio.*
import _root_.zio.test.*
import _root_.zio.test.Assertion.*
import _root_.zio.test.TestAspect.*

object MongoSpec extends ZIOSpecDefault:

  final val TIMEOUT   = 1.second
  final val TEST_DB   = "rednova_mongo_test"
  final val TEST_URI  = "mongodb://localhost:27017"
  final val collNames = Chunk("coll_1", "coll_2")

  def spec = suite("MongoSpec")(
    test("healthcheck") {
      for
        db   <- Mongo.getDatabase(TEST_DB)
        rslt <- Mongo.healthcheck(db).either
      yield assert(rslt)(isRight)
    }.provide(Mongo.live(TEST_URI)),
    test("ping") {
      for
        db   <- Mongo.getDatabase(TEST_DB)
        rslt <- Mongo.ping(db).either
      yield assert(rslt)(isRight)
    }.provide(Mongo.live(TEST_URI)),
    test("findCollectionNames") {
      for
        db   <- Mongo.getDatabase(TEST_DB)
        old  <- Mongo.getCollections(collNames)(db)
        _    <- Mongo.dropCollections(old)
        _    <- Mongo.createCollections(collNames)(db)
        rslt <- Mongo.findCollectionNames(db)
      yield assert(rslt)(hasSubset(collNames))
    }.provide(Mongo.live(TEST_URI)),
  )
