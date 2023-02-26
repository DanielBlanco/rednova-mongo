package sample

import com.mongodb.MongoException
import com.mongodb.client.result.*
import java.time.{ Instant, LocalDate }
import java.util.UUID
import mongo4cats.bson.*
import mongo4cats.bson.syntax.*
import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.operations.*
import mongo4cats.zio.*
import mongo4cats.zio.JsonCodecs.{ *, given }
import scala.reflect.ClassTag
import _root_.zio.*
import _root_.zio.json.*

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
    updatedAt: LocalDate = LocalDate.now(),
  ) extends MongoDoc
object Item:
  implicit val jsonDecoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
  implicit val jsonEncoder: JsonEncoder[Item] = DeriveJsonEncoder.gen[Item]

final case class ItemRepo(repo: MongoRepo[Item])
object ItemRepo:
  type ItemRepoIO[A] = RIO[ItemRepo, A]

  def layer(db: String): URLayer[Mongo, ItemRepo] = ZLayer {
    for mongo <- ZIO.service[Mongo]
    yield ItemRepo(MongoRepo[Item](mongo, db, "items"))
  }

  def clearCollection: ItemRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.repo.clearCollection)

  def count: ItemRepoIO[Long] =
    ZIO.serviceWithZIO(_.repo.count)

  def count(filter: Filter): ItemRepoIO[Long] =
    ZIO.serviceWithZIO(_.repo.count(filter))

  def explain(filter: Filter): ItemRepoIO[Document] =
    ZIO.serviceWithZIO(_.repo.explain(filter))

  def aggregate[Y: ClassTag: MongoCodecProvider](
      agg: Aggregate
    ): ItemRepoIO[Chunk[Y]] =
    ZIO.serviceWithZIO(_.repo.aggregate[Y](agg))

  def finder: ItemRepoIO[Finder[Item]] =
    ZIO.serviceWithZIO(_.repo.finder)

  def findChunks: ItemRepoIO[Chunk[Item]] =
    ZIO.serviceWithZIO(_.repo.findChunks)

  def findChunks(filter: Filter): ItemRepoIO[Chunk[Item]] =
    ZIO.serviceWithZIO(_.repo.findChunks(filter))

  def findFirst: ItemRepoIO[Option[Item]] =
    ZIO.serviceWithZIO(_.repo.findFirst)

  def findFirst(filter: Filter): ItemRepoIO[Option[Item]] =
    ZIO.serviceWithZIO(_.repo.findFirst(filter))

  def insert(doc: Item): ItemRepoIO[Item] =
    for
      repo    <- ZIO.service[ItemRepo].map(_.repo)
      _       <- repo.insert(doc)
      opt     <- repo.findFirst(Filter.idEq(doc._id))
      updated <- ZIO
                   .fromOption(opt)
                   .mapError(_ => new MongoException("Inserted item not found"))
    yield updated

  def insertMany(docs: Chunk[Item]): ItemRepoIO[InsertManyResult] =
    ZIO.serviceWithZIO(_.repo.insertMany(docs))

  def remove(id: ObjectId): ItemRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.repo.remove(id))

  def remove(filter: Filter): ItemRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.repo.remove(filter))

  def removeAll: ItemRepoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.repo.clearCollection)

  def translate(filter: Filter): ItemRepoIO[String] =
    ZIO.serviceWithZIO(_.repo.translate(filter))

  def update(doc: Item): ItemRepoIO[UpdateResult] =
    ZIO.serviceWithZIO(_.repo.update(doc))

  def update(query: Filter, update: Update): ItemRepoIO[UpdateResult] =
    ZIO.serviceWithZIO(_.repo.update(query, update))
