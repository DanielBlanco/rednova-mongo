package mongo4cats.operations

import dev.rednova.mongo.Mongo
import mongo4cats.queries.FindQueryBuilder
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import scala.jdk.CollectionConverters.*

extension (a: Filter)
  def translate: String =
    Mongo.bsonToBsonDocument(a.toBson).toString

extension (agg: Aggregate)
  def set[TExpression](
      fields: List[(String, TExpression)]
    ): Aggregate =
    AggregateBuilder(
      Aggregates.set(
        fields.map(f => new Field(f._1, f._2)).reverse.asJava
      ) :: agg.aggregates
    )

extension [A](a: FindQueryBuilder[Task, A])

  def chunks: Task[Chunk[A]] =
    a.all.map(Chunk.fromIterable)

  def zstream[R](queueSize: Int = 16): ZStream[R, Throwable, A] =
    a.stream.toZStream(queueSize)
