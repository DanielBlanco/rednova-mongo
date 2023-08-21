package mongo4cats.operations

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import scala.jdk.CollectionConverters.*
import mongo4cats.zio.Mongo

extension (
    a: Filter
  )
  def translate: String =
    Mongo.bsonToBsonDocument(a.toBson).toString

extension (
    agg: Aggregate
  )
  def set[TExpression](
      fields: List[
        (
            String,
            TExpression,
          )
      ]
    ): Aggregate =
    AggregateBuilder(
      Aggregates.set(
        fields.map(f => new Field(f._1, f._2)).reverse.asJava
      ) :: agg.aggregates
    )
