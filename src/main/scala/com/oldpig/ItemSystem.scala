package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

final case class Item(front_id: Int, name: String, description: String, user: Int, price: Double, deposit: Double,
					  image: List[String], availableTime: Int, transfer: Int)

final case class ItemPostInfo(name: String, description: String, user: Int, price: Double, deposit: Double,
							  image: List[String], availableTime: Int, transfer: Int)

object ItemSystem {
	def props = Props[ItemSystem]

	case class CreateItem(itemPostInfo: ItemPostInfo)

	case class PatchItem(item: Item)

	case class DeleteItem(item: Item)

}

class ItemSystem extends Actor with ActorLogging {
	lazy val dbSystem = context.actorSelection("../dbSystemActor")

	import ItemSystem._

	override def receive: Receive = {
		case CreateItem(itemPostInfo) =>
			sender() ! createItem(itemPostInfo)
		case PatchItem(item) =>
			sender() ! patchItem(item)
		case DeleteItem(item) =>
			sender() ! deleteItem(item)
	}

	def createItem(i: ItemPostInfo): Item = {
		Item(99999, i.name, i.description, i.user, i.price, i.deposit, i.image, i.availableTime, i.transfer)
	}

	def patchItem(item: Item): Item = {
		item
	}

	def deleteItem(item: Item): Item = {
		item
	}
}