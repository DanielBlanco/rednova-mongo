package zongo.support

import com.mongodb.MongoException
import com.mongodb.client.result.*
import java.time.{Instant, LocalDate}
import java.util.UUID
import mongo4cats.bson.*
import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.operations.*
import mongo4cats.zio.*
import scala.reflect.ClassTag
import zio.*
import zio.json.*
import zio.stream.*
import zongo.*
import zongo.json.*

case class Counter(count: Int) extends Product
object Counter:
  implicit val jsonDecoder: JsonDecoder[Counter]       = DeriveJsonDecoder.gen[Counter]
  implicit val jsonEncoder: JsonEncoder[Counter]       = DeriveJsonEncoder.gen[Counter]
  implicit val mongoCodec: MongoCodecProvider[Counter] = jsonCodecProvider[Counter]

case class Item(
    _id: ObjectId,
    uuid: UUID,
    name: String,
    createdAt: Instant = Instant.now(),
    updatedAt: LocalDate = LocalDate.now()
) extends MongoDoc
object Item:
  implicit val jsonDecoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
  implicit val jsonEncoder: JsonEncoder[Item] = DeriveJsonEncoder.gen[Item]

trait ItemsRepo:

  def clearCollection: Task[DeleteResult]

  def count: Task[Long]

  def count(filter: Filter): Task[Long]

  def explain(filter: Filter): Task[Document]

  def aggregate[Y: ClassTag: MongoCodecProvider](agg: Aggregate): Task[Chunk[Y]]

  def finder: Task[FindQueryBuilder[Item]]

  def findChunks: Task[Chunk[Item]]

  def findChunks(filter: Filter): Task[Chunk[Item]]

  def findFirst: Task[Option[Item]]

  def findFirst(filter: Filter): Task[Option[Item]]

  def insert(doc: Item): Task[Item]

  def insertMany(docs: Chunk[Item]): Task[InsertManyResult]

  def remove(id: ObjectId): Task[DeleteResult]

  def remove(filter: Filter): Task[DeleteResult]

  def removeAll: Task[DeleteResult] = clearCollection

  // def translate(filter: Filter): Task[String]

  def update(doc: Item): Task[UpdateResult]

  def update(query: Filter, update: Update): Task[UpdateResult]

object ItemsRepo:
  type ItemsRepoIO[A] = RIO[ItemsRepo, A]

  def layer(db: String): URLayer[Mongo, ItemsRepo] = ZLayer {
    for mongo <- ZIO.service[Mongo]
    yield ItemsRepoLive(MongoRepo[Item](mongo, db, "items"))
  }

  def clearCollection: ItemsRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.clearCollection)

  def count: ItemsRepoIO[Long] =
    ZIO.serviceWithZIO(_.count)

  def count(filter: Filter): ItemsRepoIO[Long] =
    ZIO.serviceWithZIO(_.count(filter))

  def explain(filter: Filter): ItemsRepoIO[Document] =
    ZIO.serviceWithZIO(_.explain(filter))

  def aggregate[Y: ClassTag: MongoCodecProvider](
      agg: Aggregate
  ): ItemsRepoIO[Chunk[Y]] =
    ZIO.serviceWithZIO(_.aggregate[Y](agg))

  def finder: ItemsRepoIO[FindQueryBuilder[Item]] =
    ZIO.serviceWithZIO(_.finder)

  def findChunks: ItemsRepoIO[Chunk[Item]] =
    ZIO.serviceWithZIO(_.findChunks)

  def findChunks(filter: Filter): ItemsRepoIO[Chunk[Item]] =
    ZIO.serviceWithZIO(_.findChunks(filter))

  def findFirst: ItemsRepoIO[Option[Item]] =
    ZIO.serviceWithZIO(_.findFirst)

  def findFirst(filter: Filter): ItemsRepoIO[Option[Item]] =
    ZIO.serviceWithZIO(_.findFirst(filter))

  def insert(doc: Item): ItemsRepoIO[Item] =
    ZIO.serviceWithZIO(_.insert(doc))

  def insertMany(docs: Chunk[Item]): ItemsRepoIO[InsertManyResult] =
    ZIO.serviceWithZIO(_.insertMany(docs))

  def remove(id: ObjectId): ItemsRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.remove(id))

  def remove(filter: Filter): ItemsRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.remove(filter))

  def removeAll: ItemsRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.removeAll)

  // def translate(filter: Filter): ItemsRepoIO[String] =
  //   ZIO.serviceWithZIO(_.translate(filter))

  def update(doc: Item): ItemsRepoIO[UpdateResult] =
    ZIO.serviceWithZIO(_.update(doc))

  def update(query: Filter, update: Update): ItemsRepoIO[UpdateResult] =
    ZIO.serviceWithZIO(_.update(query, update))

final case class ItemsRepoLive(repo: MongoRepo[Item]) extends ItemsRepo:

  def clearCollection: Task[DeleteResult] = repo.clearCollection

  def count: Task[Long] = repo.count

  def count(filter: Filter): Task[Long] = repo.count(filter)

  // def translate(filter: Filter): Task[String] = repo.translate(filter)

  def explain(filter: Filter): Task[Document] = repo.explain(filter)

  def aggregate[Y: ClassTag: MongoCodecProvider](agg: Aggregate): Task[Chunk[Y]] =
    repo.aggregate[Y](agg)

  def finder: Task[FindQueryBuilder[Item]] = repo.finder

  def findChunks: Task[Chunk[Item]] = repo.findChunks

  def findChunks(filter: Filter): Task[Chunk[Item]] = repo.findChunks(filter)

  def findFirst: Task[Option[Item]] = repo.findFirst

  def findFirst(filter: Filter): Task[Option[Item]] = repo.findFirst(filter)

  def insert(doc: Item): Task[Item] =
    for {
      _       <- repo.insert(doc)
      opt     <- repo.findFirst(Filter.idEq(doc._id))
      updated <- ZIO
                   .fromOption(opt)
                   .mapError(_ => new MongoException("Inserted item not found"))
    } yield updated

  def insertMany(docs: Chunk[Item]): Task[InsertManyResult] =
    repo.insertMany(docs)

  def remove(id: ObjectId): Task[DeleteResult] =
    repo.remove(id)

  def remove(filter: Filter): Task[DeleteResult] =
    repo.remove(filter)

  def update(doc: Item): Task[UpdateResult] =
    repo.update(doc)

  def update(query: Filter, update: Update): Task[UpdateResult] =
    repo.update(query, update)

end ItemsRepoLive
