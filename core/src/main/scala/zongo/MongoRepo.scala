package zongo

import com.mongodb.MongoException
import com.mongodb.client.result._
import org.bson.conversions.Bson
import mongo4cats.bson._
import mongo4cats.client._
import mongo4cats.database._
import mongo4cats.codecs._
import mongo4cats.collection.operations._
import scala.reflect.ClassTag
import zio._
import zio.stream._

/** Helper class to facilitate the creation of Mongo repositories. */
case class MongoRepo[D <: MongoDoc: ClassTag](
    mongo: Mongo.Service,
    databaseName: String,
    collectionName: String
)(implicit cp: MongoCodecProvider[D]) {

  /** @see MongoRepo.clearCollection */
  def clearCollection: Task[DeleteResult] =
    _coll_ >>= (c => mongo.clearCollection(c))

  /** @see MongoRepo.count */
  def count: Task[Long] =
    _coll_ >>= (_.count)

  /** @see MongoRepo.count */
  def count(filter: Filter): Task[Long] =
    _coll_ >>= (_.count(filter))

  /** @see MongoRepo.finder */
  def finder: Task[FindQueryBuilder[D]] =
    _coll_.map(_.find)

  /** Returns a query builder to find documents matching some criteria. */
  def findChunks: Task[Chunk[D]] =
    finder.flatMap(_.chunks)

  /** Returns a query builder to find documents matching some criteria. */
  def findChunks(filter: Filter): Task[Chunk[D]] =
    finder.flatMap(_.filter(filter).chunks)

  /** Returns a query builder to find documents matching some criteria. */
  def findFirst: Task[Option[D]] =
    finder.flatMap(_.first)

  /** Returns a query builder to find documents matching some criteria. */
  def findFirst(filter: Filter): Task[Option[D]] =
    finder.flatMap(_.filter(filter).first)

  /** @see MongoRepo.insert */
  def insert(doc: D): Task[InsertOneResult] =
    _coll_ >>= (_.insertOne(doc))

  /** @see MongoRepo.insertMany */
  def insertMany(docs: Chunk[D]): Task[InsertManyResult] =
    _coll_ >>= (_.insertMany(docs.toSeq))

  /** @see MongoRepo.remove */
  def remove(id: MongoId): Task[DeleteResult] =
    remove(Filter.idEq(id))

  /** @see MongoRepo.remove */
  def remove(filter: Filter): Task[DeleteResult] =
    _coll_ >>= (_.deleteMany(filter))

  /** @see MongoRepo.update */
  def update(doc: D): Task[UpdateResult] =
    for {
      c      <- _coll_
      filter  = (id: ObjectId) => Document("_id" -> id)
      update  = Document("$set", doc)
      result <- doc._id match {
                  case None     => idNotFound
                  case Some(id) => c.updateOne(filter(id), update)
                }
    } yield result

  /** @see MongoRepo.update */
  def update(
      query: Filter,
      update: Update
  ): Task[UpdateResult] =
    _coll_ >>= (_.updateMany(query, update))

  protected def _db_ =
    mongo.getDatabase(databaseName)

  protected def _coll_ =
    _db_ >>= (_.getCollectionWithCodec[D](collectionName))

  protected def idNotFound =
    Task.fail(new MongoException("Document does not have an id"))

}
