package com.oldpig

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.casbah.Imports.BasicDBList
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.query.Imports._

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

final case class Location(province: String, city: String, region: String)

final case class Item(front_id: String, name: String, description: String, user: String, price: Double,
  deposit: Double, image: List[String], startTime: Int, endTime: Int, transfer: Int,
  location: Location, category: String, phone: String)

final case class CreateItemInfo(name: String, description: String, user: String, price: Double,
  deposit: Double, image: List[String], startTime: Int, endTime: Int, transfer: Int,
  location: Location, category: String, phone: String)

final case class ItemList(list: List[Item])

//final case class ItemPostInfo(name: String, description: String, user: String, price: Double, deposit: Double,
//							  image: List[String], startTime: Int, availableTime: Int, transfer: Int,
//							  location: Location)

final case class ItemDeleteInfo(front_id: String)

object ItemSystem {
  def props = Props[ItemSystem]

  case class CreateItem(item: CreateItemInfo)

  case class PatchItem(item: Item)

  case class DeleteItem(itemDeleteInfo: ItemDeleteInfo)

  case class GetItemInfo(front_id: String)

  case class GetItemSamples(n: Int)

}

class ItemSystem extends Actor with ActorLogging {
  lazy val dbSystem = context.actorSelection("../dbSystemActor")
  implicit lazy val timeout = Timeout(5.seconds)

  import ItemSystem._

  override def receive: Receive = {
    case CreateItem(item) =>
      sender() ! createItem(item)
    case PatchItem(item) =>
      sender() ! patchItem(item)
    case DeleteItem(deleteInfo) =>
      sender() ! deleteItem(deleteInfo)
    case GetItemInfo(front_id) =>
      sender() ! getItemInfo(front_id)
    case GetItemSamples(n) =>
      sender() ! randomItems(n)
  }

  def getItemInfo(front_id: String): Item = {
    val query = MongoDBObject("_id" -> new ObjectId(front_id))
    val f1 = (dbSystem ? DBSystem.Query("item", query)).mapTo[Array[DBObject]]
    val result = Await.result(f1, Duration.Inf)
    if (result.isEmpty) Item("null", "null", "null", "null", 0, 0, List("null"),
      0, 0, 0, Location("null", "null", "null"), "null", "0")
    else {
      val item = result(0)
      val locList = (List() ++ item("location").asInstanceOf[BasicDBList]) map {
        _.asInstanceOf[String]
      }
      Item(
        item("_id").toString,
        item.get("name").toString,
        item.get("description").toString,
        item.get("user").toString,
        item.get("price").toString.toDouble,
        item.get("deposit").toString.toDouble,
        (List() ++ item("image").asInstanceOf[BasicDBList]) map {
          _.asInstanceOf[String]
        },
        item.get("startTime").toString.toInt,
        item.get("endTime").toString.toInt,
        item.get("transfer").toString.toInt,
        Location(locList(0), locList(1), locList(2)),
        item.get("category").toString,
        item.get("phone").toString)
    }
  }

  def createItem(i: CreateItemInfo): PatchResult = {
    val content = MongoDBObject(
      "name" -> i.name,
      "description" -> i.description,
      "user" -> i.user,
      "price" -> i.price,
      "deposit" -> i.deposit,
      "image" -> i.image,
      "startTime" -> i.startTime,
      "endTime" -> i.endTime,
      "transfer" -> i.transfer,
      "location" -> i.location,
      "category" -> i.category,
      "phone" -> i.phone)
    val f1 = (dbSystem ? DBSystem.Insert("item", content)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def patchItem(i: Item): PatchResult = {
    val query = MongoDBObject("_id" -> new ObjectId(i.front_id))
    val content = $set(
      "name" -> i.name,
      "description" -> i.description,
      "user" -> i.user,
      "price" -> i.price,
      "deposit" -> i.deposit,
      "image" -> i.image,
      "startTime" -> i.startTime,
      "endTime" -> i.endTime,
      "transfer" -> i.transfer,
      "location" -> i.location,
      "category" -> i.category,
      "phone" -> i.phone)
    val f1 = (dbSystem ? DBSystem.Update("item", query, content)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def deleteItem(info: ItemDeleteInfo): PatchResult = {
    val query = MongoDBObject("_id" -> new ObjectId(info.front_id))
    val f1 = (dbSystem ? DBSystem.Delete("item", query)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def randomItems(n: Int): ItemList = {
    val f1 = (dbSystem ? DBSystem.Sample("item", n)).mapTo[Array[DBObject]]
    val results = Await.result(f1, Duration.Inf)
    var list = List[Item]()
    for (item <- results) {
      val locList = (List() ++ item("location").asInstanceOf[BasicDBList]) map {
        _.asInstanceOf[String]
      }
      list ::= Item(
        item("_id").toString,
        item.get("name").toString,
        item.get("description").toString,
        item.get("user").toString,
        item.get("price").toString.toDouble,
        item.get("deposit").toString.toDouble,
        (List() ++ item("image").asInstanceOf[BasicDBList]) map {
          _.asInstanceOf[String]
        },
        //				item.as[List[String]]("image"),
        item.get("startTime").toString.toInt,
        item.get("endTime").toString.toInt,
        item.get("transfer").toString.toInt,
        //				item.as[Location]("location"),
        Location(locList(0), locList(1), locList(2)),
        item.get("category").toString,
        item.get("phone").toString)
    }
    ItemList(list)
  }
}
