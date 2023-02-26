package mongo4cats.zio.internal

import com.mongodb.{ ReadConcern, ReadPreference, WriteConcern }
import com.mongodb.client.result.{ DeleteResult, InsertManyResult, InsertOneResult, UpdateResult }
import mongo4cats.bson.Document
import mongo4cats.bson.syntax.*
import mongo4cats.codecs.CodecRegistry
import mongo4cats.zio.*
import org.bson.conversions.Bson
import scala.reflect.ClassTag
import zio.*
import zio.prelude.*

final case class MongoLive(
    val client: ZMongoClient
  ) extends Mongo:

  /** @see Mongo.Service.database */
  def getDatabase(name: String): Task[ZMongoDatabase] =
    client.getDatabase(name)

  /** @see Mongo.Service.dropDatabase */
  def dropDatabase(db: ZMongoDatabase): Task[Unit] =
    db.drop

  /** @see Mongo.Service.runCommand */
  def runCommand(
      command: Bson
    )(
      db: ZMongoDatabase
    ): Task[Document] =
    db.runCommand(command)

  /** @see Mongo.Service.runCommand */
  def runCommand(
      command: Bson,
      readPreference: ReadPreference,
    )(
      db: ZMongoDatabase
    ): Task[Document] =
    db.runCommand(command, readPreference)

  /** @see Mongo.Service.findCollectionNames */
  def findCollectionNames(db: ZMongoDatabase): Task[Chunk[String]] =
    db.listCollectionNames.map(_.toChunk)

  /** @see Mongo.Service.clearCollection */
  def clearCollection[A](c: ZMongoCollection[A]): Task[DeleteResult] =
    c.deleteMany(Document())

  /** @see Mongo.Service.getCollection */
  def getCollection(
      name: String
    )(
      db: ZMongoDatabase
    ): Task[ZMongoCollection[Document]] =
    db.getCollection(name)

  /** @see Mongo.Service.getCollection */
  def getCollection[A: ClassTag](
      name: String,
      codecRegistry: CodecRegistry,
    )(
      db: ZMongoDatabase
    ): Task[ZMongoCollection[A]] =
    db.getCollection(name, codecRegistry)

  def dropCollection[A](c: ZMongoCollection[A]): Task[Unit] =
    c.drop

object MongoLive:

  def apply(uri: String) =
    connect(uri).map(client => new MongoLive(client))

  private def connect(uri: String) =
    ZMongoClient.fromConnectionString(uri)
