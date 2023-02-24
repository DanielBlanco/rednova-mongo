package zongo.internal

import com.mongodb.ReadPreference
import com.mongodb.client.result.DeleteResult
import org.bson.conversions.Bson
import mongo4cats.bson.*
import mongo4cats.codecs.CodecRegistry
import mongo4cats.zio.*
import mongo4cats.zio.ZMongoClient as MongoClient
import mongo4cats.zio.ZMongoDatabase as MongoDatabase
import mongo4cats.zio.ZMongoCollection as MongoCollection
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import zio.*
import zio.prelude.*
import zio.stream.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import zio.interop.reactivestreams.*
import zio.stream.interop.fs2z.*
import zongo.Mongo

final case class MongoLive(
    val client: MongoClient
) extends Mongo:

  /** @see Mongo.Service.database */
  def getDatabase(name: String): Task[MongoDatabase] =
    client.getDatabase(name)

  /** @see Mongo.Service.runCommand */
  def runCommand(
      command: Bson
  )(db: MongoDatabase): Task[Document] =
    db.runCommand(command)

  /** @see Mongo.Service.runCommand */
  def runCommand(
      command: Bson,
      readPreference: ReadPreference
  )(db: MongoDatabase): Task[Document] =
    db.runCommand(command, readPreference)

  /** @see Mongo.Service.findCollectionNames */
  def findCollectionNames(db: MongoDatabase): Task[Chunk[String]] =
    db.listCollectionNames.map(_.toChunk)

  /** @see Mongo.Service.clearCollection */
  def clearCollection[A](c: MongoCollection[A]): Task[DeleteResult] =
    c.deleteMany(Document())

  /** @see Mongo.Service.getCollection */
  def getCollection(
      name: String
  )(db: MongoDatabase): Task[MongoCollection[Document]] =
    db.getCollection(name)

  /** @see Mongo.Service.getCollection */
  def getCollection[A: ClassTag](
      name: String,
      codecRegistry: CodecRegistry
  )(db: MongoDatabase): Task[MongoCollection[A]] =
    db.getCollection(name, codecRegistry)

  def dropCollection[A](c: MongoCollection[A]): Task[Unit] =
    c.drop

object MongoLive:

  def apply(uri: String)           =
    connect(uri).map(client => new MongoLive(client))

  private def connect(uri: String) =
    ZMongoClient.fromConnectionString(uri)
