package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

import scala.language.reflectiveCalls
import scala.util.Random

object DBSystem {
    def props = Props[DBSystem]

    case class DebugMsg(msg: String)

    case class DebugReturnInt(x: Int)

    case class Insert(coll: String, content: MongoDBObject)

    case class Update(coll: String, query: MongoDBObject, up: MongoDBObject)

    case class Delete(coll: String, query: MongoDBObject)

    case class Query(coll: String, condition: MongoDBObject)

    case class Sample(coll: String, n: Int)

}

class DBSystem extends Actor with ActorLogging {

    import DBSystem._

    lazy val mongoClient = MongoClient("localhost", 27017)
    lazy val db = mongoClient("oldpig")

    /**
      * event handling
      * @return nothing
      */
    override def receive: Receive = {
        case Insert(coll, content) =>
            sender() ! insert(coll, content)
        case Update(coll, query, up) =>
            sender() ! update(coll, query, up)
        case Delete(coll, query) =>
            sender() ! delete(coll, query)
        case Query(coll, condition) =>
            sender() ! query(coll, condition)
        case DebugMsg(msg) =>
            println(msg)
            sender() ! msg
        case DebugReturnInt(x) =>
            sender() ! x
        case Sample(coll, n) =>
            sender() ! sample(coll, n)
    }

    /**
      * query
      * @param coll collection name
      * @param condition query condition
      * @return query result
      */
    def query(coll: String, condition: MongoDBObject): Array[DBObject] = {
        db(coll).find(condition).toArray
    }

    /**
      * insert
      * @param coll collection name
      * @param content insert content
      * @return insert result
      */
    def insert(coll: String, content: MongoDBObject): String = {
        val result = db(coll).insert(content)
        println("insert " + result.getN)
        content.get("_id").get.toString
    }

    /**
      * update
      * @param coll collection name
      * @param query update condition
      * @param up update content
      * @return update result
      */
    def update(coll: String, query: MongoDBObject, up: MongoDBObject): String = {
        val result = db(coll).update(query, up)
        println("update " + result.getN)
        "Update %d document(s).".format(result.getN)
    }

    /**
      * delete
      * @param coll collection name
      * @param query delete condition
      * @return delete result
      */
    def delete(coll: String, query: MongoDBObject): String = {
        val result = db(coll).remove(query)
        println("remove " + result.getN)
        "Delete %d document(s)".format(result.getN)
    }

    /**
      * random sampling
      * @param coll collection name
      * @param n number of documents
      * @return an array of objects
      */
    def sample(coll: String, n: Int): Array[DBObject] = {
        val all = db(coll).find()
        if (all.size < n)
            all.toArray
        else {
            Random.shuffle(all).toArray.take(n)
        }
    }
}
