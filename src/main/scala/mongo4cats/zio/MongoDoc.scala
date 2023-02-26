package mongo4cats.zio

import mongo4cats.bson.*

abstract class MongoDoc extends Product:

  def _id: ObjectId
