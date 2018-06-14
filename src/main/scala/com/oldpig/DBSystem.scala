package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.{Cursor, MongoClient, MongoDB}

import scala.language.reflectiveCalls

object DBSystem {
	def props = Props[DBSystem]

	case class DebugMsg(msg: String)

	case class DebugReturnInt(x: Int)

	case class Insert(coll: String, content: MongoDBObject)

	case class Update(coll: String, query: MongoDBObject, up: MongoDBObject)

	case class Delete(coll: String, query: MongoDBObject)

	case class Query(coll: String, condition: MongoDBObject)

}

class DBSystem extends Actor with ActorLogging {

	import DBSystem._

	lazy val mongoClient = MongoClient("localhost", 27017)
	lazy val db = mongoClient("oldpig")
//	val coll = db("test")
//	val a = MongoDBObject("k1" -> "v1", "k2" -> 2)
//	val b = MongoDBObject("k1" -> "v1", "k2" -> 3)
//	coll.insert(a)
//	coll.insert(b)
//	coll.insert(MongoDBObject("k1" -> "v1", "k2" -> 3))
//	for (i <- coll.find.toArray) println(i.get("k2"))
//	println("=====")
//	val result = coll.remove(MongoDBObject("k1" -> "v1"))
//	println(result.getN)
//	for (i <- coll.find) println(i)
//	println("=====")
//	val b = MongoDBObject("hi" -> "bye")
//	println(a)
//	coll.save(a)
//	coll.insert(b)
//
//	println(db.collectionNames())
//	println(coll.count())
//	val allDocs = coll.find(a)
//	for (i <- allDocs) println(i)
//val query = MongoDBObject("hello" -> "world")
//	val upd = $set("platform" -> "JVM")
//	val result = coll.update(query, upd, multi = true)
//	println("Number updated: " + result.getN)
//	for (c <- coll.find) println(c)

//	val query = MongoDBObject()
//	val result = coll.remove(query)
//	println("Number removed: " + result.getN)
//	for (c <- coll.find) println(c)

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
	}

	def query(coll: String, condition: MongoDBObject): Array[DBObject] = {
		db(coll).find(condition).toArray
	}

	def insert(coll: String, content: MongoDBObject): String = {
		db(coll).insert(content)
		"Success."
	}

	def update(coll: String, query: MongoDBObject, up: MongoDBObject): String = {
		db(coll).update(query, up)
		"Success."
	}

	def delete(coll: String, query: MongoDBObject): String = {
		db(coll).remove(query)
		"Success."
	}
}
