package dev.rednova.mongo.support

import dev.rednova.mongo.Mongo
import zio.*

type SpecEnv = SpecConfig & Mongo
