package dev.rednova.mongo

import zio.*
import zio.prelude.*
import zio.prelude.Assertion.*

/** New MongoUri type. */
object MongoUri extends Subtype[String]:

  inline override def assertion =
    startsWith("mongodb")

type MongoUri = MongoUri.Type
