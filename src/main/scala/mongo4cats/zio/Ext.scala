package mongo4cats.zio

import mongo4cats.operations.*
import scala.jdk.CollectionConverters.*
import zio.*

extension [A](a: Queries.Find[A])
  def chunks: Task[Chunk[A]] =
    a.all.map(Chunk.fromIterable)
