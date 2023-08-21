package mongo4cats.zio

import mongo4cats.bson.ObjectId
import mongo4cats.operations.*
import com.mongodb.client.model.Filters
import java.util.UUID
import sample.*
import scala.util.chaining.*
import _root_.zio.*
import _root_.zio.test.*
import _root_.zio.test.Assertion.*
import _root_.zio.test.TestAspect.*

object MongoRepoSpec extends ZIOSpecDefault:

  lazy val repo =
    Mongo.live(MongoSpec.TEST_URI) >>>
      ItemRepo.layer(MongoSpec.TEST_DB)

  def spec = suite("MongoRepoSpec")(
    test("explain works") {
      for
        _    <- ItemRepo.removeAll
        _    <- ItemRepo.insertMany(bulkInsertData)
        expl <- ItemRepo.explain(inName("Luis", "John"))
        xpct  = "\"filter\": {\"name\": {\"$in\": [\"John\", \"Luis\"]}}"
      yield assert(expl.toString)(containsString(xpct))
    }.provide(repo),
    test("aggregate works") {
      for
        _      <- ItemRepo.removeAll
        _      <- ItemRepo.insertMany(bulkInsertData)
        agg     = Accumulator.sum("count", 1).pipe(Aggregate.group("$name", _))
        counts <- ItemRepo.aggregate[Counter](agg)
      yield assert(counts.size)(equalTo(3)) &&
      assert(counts.map(_.count))(equalTo(Chunk(1, 1, 1)))
    }.provide(repo),
    test("find works") {
      for
        _     <- ItemRepo.removeAll
        _     <- ItemRepo.insertMany(bulkInsertData)
        items <- ItemRepo.findChunks(inName("Luis", "John"))
        names  = items.map(_.name)
      yield assert(items.size)(equalTo(1)) &&
      assert(names)(equalTo(Chunk("John")))
    }.provide(repo),
    test("find by UUID works") {
      for
        _       <- ItemRepo.removeAll
        _       <- ItemRepo.insertMany(bulkInsertData)
        uuid     = UUID.fromString(UUID1)
        itemOpt <- ItemRepo.findFirst(Filter.eq("uuid", UUID1))
        query   <- ItemRepo.translate(Filter.eq("uuid", UUID1))
      yield assert(itemOpt.map(_.uuid))(isSome(equalTo(uuid)))
    }.provide(repo),
    test("findFirst works") {
      for
        _       <- ItemRepo.removeAll
        _       <- ItemRepo.insertMany(bulkInsertData)
        luisOpt <- ItemRepo.findFirst(byName("Daniel"))
      yield assert(luisOpt.map(_.name))(isSome(equalTo("Daniel")))
    }.provide(repo),
    test("insert works") {
      for
        _     <- ItemRepo.removeAll
        id     = oid("607ebd5d1c8f40252380ea44")
        item   = Item(id, UUID.randomUUID, "Lorelai")
        _     <- ItemRepo.insert(item)
        count <- ItemRepo.count
        found <- ItemRepo.findFirst(Filter.idEq(id))
      yield assert(count)(equalTo(1L)) &&
      assert(found.map(_.name))(isSome(equalTo("Lorelai")))
    }.provide(repo),
    test("remove works") {
      for
        _     <- ItemRepo.removeAll
        _     <- ItemRepo.insertMany(bulkInsertData)
        _     <- ItemRepo.remove(inName("Daniel", "John"))
        count <- ItemRepo.count
      yield assert(count)(equalTo(1L))
    }.provide(repo),
    test("update works") {
      for
        _      <- ItemRepo.removeAll
        _      <- ItemRepo.insertMany(bulkInsertData)
        u       = Update.set("name", "Daniela")
        _      <- ItemRepo.update(byName("Daniel"), u)
        countA <- ItemRepo.count(byName("Daniela"))
        countB <- ItemRepo.count(byName("Daniel"))
        countC <- ItemRepo.count(byName("John"))
      yield assert(countA)(equalTo(1L)) &&
      assert(countB)(equalTo(0L)) &&
      assert(countC)(equalTo(1L))
    }.provide(repo),
    test("update document works") {
      for
        _      <- ItemRepo.removeAll
        _      <- ItemRepo.insertMany(bulkInsertData)
        finder <- ItemRepo.finder
        docOpt <- finder.filter(byName("Daniel")).first
        doc    <- ZIO.fromOption(docOpt)
        doc2    = doc.copy(name = "Daniela")
        _      <- ItemRepo.update(doc2)
        countA <- ItemRepo.count(byName("Daniela"))
        countB <- ItemRepo.count(byName("Daniel"))
      yield assert(countA)(equalTo(1L)) &&
      assert(countB)(equalTo(0L))
    }.provide(repo),
  ) @@ sequential

  def byName(
      name: String
    ) =
    Filter.eq("name", name)

  def inName(
      names: String*
    ) =
    Filter.in("name", names)

  val UUID1 = "c6ac38e9-1417-49bd-ab5b-a6081eb40d71"

  def bulkInsertData = Chunk(
    Item(oid, UUID.fromString(UUID1), "Daniel"),
    Item(oid, UUID.randomUUID, "Jane"),
    Item(oid, UUID.randomUUID, "John"),
  )

  private def oid: ObjectId = ObjectId.gen
  private def oid(
      id: String
    ): ObjectId = ObjectId(id)
