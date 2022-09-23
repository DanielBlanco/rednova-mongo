package zongo

import mongo4cats.bson.*

abstract class MongoDoc extends Product:

  def _id: ObjectId
