package mongo4cats.zio

import com.mongodb.MongoException
import com.mongodb.client.result.*
import org.bson.BsonDocument
import org.bson.conversions.Bson
import mongo4cats.bson.*
import mongo4cats.bson.syntax.*
import mongo4cats.client.*
import mongo4cats.database.*
import mongo4cats.codecs.*
import mongo4cats.operations.*
import mongo4cats.zio.*
import mongo4cats.zio.json.*
import scala.reflect.ClassTag
import _root_.zio.*
import _root_.zio.stream.*
import _root_.zio.json.*

type Finder[A] = Queries.Find[A]

/** Helper class to facilitate the creation of Mongo repositories. */
case class MongoRepo[D <: MongoDoc: ClassTag](
    mongo: Mongo,
    databaseName: String,
    collectionName: String,
  )(using
    cp: MongoCodecProvider[D],
    enc: JsonEncoder[D],
  ):

  /** @see MongoRepo.clearCollection */
  def clearCollection: Task[DeleteResult] =
    getCollection.flatMap(c => mongo.clearCollection(c))

  /** @see MongoRepo.count */
  def count: Task[Long] =
    getCollection.flatMap(_.count)

  /** @see MongoRepo.count */
  def count(
      filter: Filter
    ): Task[Long] =
    getCollection.flatMap(_.count(filter))

  /** Explain the execution plan for this operation with the server's default verbosity level.
    */
  def explain(
      filter: Filter
    ): Task[Document] =
    finder.flatMap(_.filter(filter).explain)

  /** Convert a filter into a string which can then be printed. */
  def translate(
      filter: Filter
    ): Task[String] =
    ZIO.attempt(filter.translate)

  /** Allow us to run an aggregate. */
  def aggregate[Y: ClassTag: MongoCodecProvider](
      agg: Aggregate
    ): Task[Chunk[Y]] =
    getCollection.flatMap(
      _.aggregateWithCodec[Y](agg).all.map(Chunk.fromIterable)
    )

  /** Returns a finder object */
  def finder: Task[Finder[D]] =
    getCollection.map(_.find)

  /** Returns a query builder to find documents matching some criteria. */
  def findChunks: Task[Chunk[D]] =
    finder.flatMap(_.all.map(Chunk.fromIterable))

  /** Returns a query builder to find documents matching some criteria. */
  def findChunks(
      filter: Filter
    ): Task[Chunk[D]] =
    finder.flatMap(_.filter(filter).all.map(Chunk.fromIterable))

  /** Returns a query builder to find documents matching some criteria. */
  def findFirst: Task[Option[D]] =
    finder.flatMap(_.first)

  /** Returns a query builder to find documents matching some criteria. */
  def findFirst(
      filter: Filter
    ): Task[Option[D]] =
    finder.flatMap(_.filter(filter).first)

  /** @see MongoRepo.insert */
  def insert(
      doc: D
    ): Task[InsertOneResult] =
    getCollection.flatMap(_.insertOne(doc))

  /** @see MongoRepo.insertMany */
  def insertMany(
      docs: Chunk[D]
    ): Task[InsertManyResult] =
    getCollection.flatMap(_.insertMany(docs.toSeq))

  /** @see MongoRepo.remove */
  def remove(
      id: ObjectId
    ): Task[DeleteResult] =
    remove(Filter.idEq(id))

  /** @see MongoRepo.remove */
  def remove(
      filter: Filter
    ): Task[DeleteResult] =
    getCollection.flatMap(_.deleteMany(filter))

  /** @see MongoRepo.update */
  def update(
      doc: D
    ): Task[UpdateResult] =
    for
      c       <- getCollection
      filter   = (id: ObjectId) => Document("_id" := id)
      document = Document.parse(doc.toJson)
      update   = Document("$set" -> BsonValue.document(document))
      result  <- c.updateOne(filter(doc._id), update)
    yield result

  /** @see MongoRepo.update */
  def update(
      query: Filter,
      update: Update,
    ): Task[UpdateResult] =
    getCollection.flatMap(_.updateMany(query, update))

  def getDatabase =
    mongo.getDatabase(databaseName)

  def getCollection =
    getDatabase.flatMap(_.getCollectionWithCodec[D](collectionName))
