package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

final case class Item(front_id: String, name: String, description: String, user: String, price: Double, deposit: Double,
					  image: List[String], availableTime: Int, transfer: Int)

final case class ItemPostInfo(name: String, description: String, user: String, price: Double, deposit: Double,
							  image: List[String], availableTime: Int, transfer: Int)

final case class ItemDeleteInfo(front_id: String)

object ItemSystem {
	def props = Props[ItemSystem]

	case class CreateItem(item: Item)

	case class PatchItem(item: Item)

	case class DeleteItem(itemDeleteInfo: ItemDeleteInfo)

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
	}

	def createItem(i: Item): PatchResult = {
		val content = MongoDBObject(
			"front_id" -> i.front_id,
			"name" -> i.name,
			"description" -> i.description,
			"user" -> i.user,
			"price" -> i.price,
			"deposit" -> i.deposit,
			"image" -> i.image,
			"availableTime" -> i.availableTime,
			"transfer" -> i.transfer
		)
		val f1 = (dbSystem ? DBSystem.Insert("item", content)).mapTo[String]
		PatchResult(Await.result(f1, Duration.Inf))
	}

	def patchItem(i: Item): PatchResult = {
		val query = MongoDBObject("front_id" -> i.front_id)
		val content = $set(
			"name" -> i.name,
			"description" -> i.description,
			"user" -> i.user,
			"price" -> i.price,
			"deposit" -> i.deposit,
			"image" -> i.image,
			"availableTime" -> i.availableTime,
			"transfer" -> i.transfer
		)
		val f1 = (dbSystem ? DBSystem.Update("item", query, content)).mapTo[String]
		PatchResult(Await.result(f1, Duration.Inf))
	}

	def deleteItem(info : ItemDeleteInfo): PatchResult = {
		val query = MongoDBObject("front_id" -> info.front_id)
		val f1 = (dbSystem ? DBSystem.Delete("item", query)).mapTo[String]
		PatchResult(Await.result(f1, Duration.Inf))
	}
}