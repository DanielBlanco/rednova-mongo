package mongo4cats.zio

import com.mongodb.{ ReadConcern, ReadPreference, WriteConcern }
import com.mongodb.client.result.{ DeleteResult, InsertManyResult, InsertOneResult, UpdateResult }
import mongo4cats.bson.Document
import mongo4cats.bson.syntax.*
import mongo4cats.codecs.CodecRegistry
import mongo4cats.zio.internal.MongoLive
import org.bson.{ BsonDocument, UuidRepresentation }
import org.bson.conversions.Bson
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import scala.reflect.ClassTag
import zio.*

trait Mongo:

  /** Gets the database with the given name.
    *
    * @param name
    *   the name of the database
    * @return
    *   the database wrapped in a ZIO.
    */
  def getDatabase(name: String): Task[ZMongoDatabase]

  /** Drops the database.
    *
    * @param db
    *   the database to drop.
    * @return
    *   unit.
    */
  def dropDatabase(db: ZMongoDatabase): Task[Unit]

  /** Clears the data from all Mongo collections. */
  def clearDatabase(db: ZMongoDatabase): Task[Unit] =
    for
      names <- findCollectionNames(db)
      colls <- getCollections(names)(db)
      _     <- clearCollections(colls)
    yield ()

  /** Find the available collections. */
  def findCollectionNames(db: ZMongoDatabase): Task[Chunk[String]]

  /** Executes command in the context of the current database.
    *
    * @param command
    *   the command to be run
    * @param db
    *   the database to use.
    * @return
    *   a ZIO Stream containing the command result.
    */
  def runCommand(command: Bson)(db: ZMongoDatabase): Task[Document]

  /** Executes command in the context of the current database.
    *
    * @param command
    *   the command to be run
    * @param readPreference
    *   the ReadPreference to be used when executing the command
    * @param db
    *   the database to use.
    * @return
    *   a ZIO Stream containing the command result.
    */
  def runCommand(
      command: Bson,
      readPreference: ReadPreference,
    )(
      db: ZMongoDatabase
    ): Task[Document]

  /** Same as ping.
    *
    * @param db
    *   the database to use.
    * @return
    *   nothing useful.
    */
  def healthcheck(db: ZMongoDatabase): Task[Unit] =
    ping(db).map(_ => ())

  /** Runs a query and if no error is returned all is good.
    *
    * @param db
    *   the database to use.
    * @return
    *   pong
    */
  def ping(db: ZMongoDatabase): Task[String] =
    runCommand(Document("ping" := "ping"))(db).map(_ => "pong")

  /** Create a mongo collection.
    *
    * @param name
    *   the name of the mongo collection to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def createCollection(
      name: String
    )(
      db: ZMongoDatabase
    ): Task[Unit] =
    runCommand(Document("create" := name))(db).map(_ => ())

  /** Create a chunk of mongo collections.
    *
    * @param name
    *   the names of the mongo collections to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def createCollections(
      names: Chunk[String]
    )(
      db: ZMongoDatabase
    ): Task[Unit] =
    ZIO.foreachPar(names)(createCollection(_)(db)).map(_ => ())

  /** Removes ALL records from a mongo collection.
    *
    * @param name
    *   the names of the mongo collections to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def clearCollection[A](c: ZMongoCollection[A]): Task[DeleteResult]

  /** Removes ALL records from chunk of mongo collection.
    *
    * @param name
    *   the names of the mongo collections to create.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def clearCollections[A](
      cs: Chunk[ZMongoCollection[A]]
    ): Task[Chunk[DeleteResult]] =
    ZIO.foreachPar(cs)(clearCollection)

  /** Gets the Mongo collection to use.
    *
    * @param name
    *   the name of the mongo collection to fetch.
    * @param db
    *   the database to use.
    * @return
    *   a ZMongoCollection instance.
    */
  def getCollection(
      name: String
    )(
      db: ZMongoDatabase
    ): Task[ZMongoCollection[Document]]

  /** Gets the Mongo collection to use for some Type.
    *
    * @param name
    *   the name of the mongo collection to fetch.
    * @param db
    *   the database to use.
    * @return
    *   a ZMongoCollection instance.
    */
  def getCollection[A: ClassTag](
      name: String,
      codecRegistry: CodecRegistry,
    )(
      db: ZMongoDatabase
    ): Task[ZMongoCollection[A]]

  /** Gets the Mongo collections.
    *
    * @param name
    *   the chunk of names to fetch.
    * @param db
    *   the database to use.
    * @return
    *   a ZMongoCollection instance.
    */
  def getCollections(
      names: Chunk[String]
    )(
      db: ZMongoDatabase
    ): Task[Chunk[ZMongoCollection[Document]]] =
    ZIO.foreachPar(names)(getCollection(_)(db))

  /** Drops a mongo collection from database.
    *
    * @param c
    *   the mongo collection to drop.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def dropCollection[A](c: ZMongoCollection[A]): Task[Unit]

  /** Drops a chunk of mongo collections from database.
    *
    * @param cs
    *   the chunk of collections to drop.
    * @param db
    *   the database to use.
    * @return
    *   unit.
    */
  def dropCollections[A](cs: Chunk[ZMongoCollection[A]]): Task[Unit] =
    ZIO.foreachPar(cs)(dropCollection(_)).map(_ => ())

/** Defines accesor & helper methods */
object Mongo:

  type MongoIO[A] = RIO[Mongo, A]

  /** Constructs a layer from a MongoClient.
    *
    * @param uri
    *   to connect.
    * @return
    *   a ZLayer.
    */
  def live(uri: String): ULayer[Mongo] =
    ZLayer.scoped(MongoLive(uri)).orDie

  /** @note This will help us debug queries. */
  lazy val bsonToBsonDocument: Bson => BsonDocument =
    _.toBsonDocument(
      classOf[BsonDocument],
      CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
        Bson.DEFAULT_CODEC_REGISTRY,
      ),
    )

  def getDatabase(name: String): MongoIO[ZMongoDatabase] =
    ZIO.serviceWithZIO(_.getDatabase(name))

  def dropDatabase(db: ZMongoDatabase): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.dropDatabase(db))

  def clearDatabase(db: ZMongoDatabase): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.clearDatabase(db))

  def findCollectionNames(db: ZMongoDatabase): MongoIO[Chunk[String]] =
    ZIO.serviceWithZIO(_.findCollectionNames(db))

  def runCommand(command: Bson)(db: ZMongoDatabase): MongoIO[Document] =
    ZIO.serviceWithZIO(_.runCommand(command)(db))

  def runCommand(
      command: Bson,
      readPreference: ReadPreference,
    )(
      db: ZMongoDatabase
    ): MongoIO[Document] =
    ZIO.serviceWithZIO(_.runCommand(command, readPreference)(db))

  def healthcheck(db: ZMongoDatabase): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.healthcheck(db))

  def ping(db: ZMongoDatabase): MongoIO[String] =
    ZIO.serviceWithZIO(_.ping(db))

  def createCollection(
      name: String
    )(
      db: ZMongoDatabase
    ): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.createCollection(name)(db))

  def createCollections(
      names: Chunk[String]
    )(
      db: ZMongoDatabase
    ): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.createCollections(names)(db))

  def clearCollection[A](c: ZMongoCollection[A]): MongoIO[DeleteResult] =
    ZIO.serviceWithZIO(_.clearCollection(c))

  def clearCollections[A](
      cs: Chunk[ZMongoCollection[A]]
    ): MongoIO[Chunk[DeleteResult]] =
    ZIO.serviceWithZIO(_.clearCollections(cs))

  def getCollection(
      name: String
    )(
      db: ZMongoDatabase
    ): MongoIO[ZMongoCollection[Document]] =
    ZIO.serviceWithZIO(_.getCollection(name)(db))

  def getCollection[A: ClassTag](
      name: String,
      codecRegistry: CodecRegistry,
    )(
      db: ZMongoDatabase
    ): MongoIO[ZMongoCollection[A]] =
    ZIO.serviceWithZIO(_.getCollection(name, codecRegistry)(db))

  def getCollections(
      names: Chunk[String]
    )(
      db: ZMongoDatabase
    ): MongoIO[Chunk[ZMongoCollection[Document]]] =
    ZIO.serviceWithZIO(_.getCollections(names)(db))

  def dropCollection[A](c: ZMongoCollection[A]): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.dropCollection(c))

  def dropCollections[A](cs: Chunk[ZMongoCollection[A]]): MongoIO[Unit] =
    ZIO.serviceWithZIO(_.dropCollections(cs))
