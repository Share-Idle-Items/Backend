package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}

final case class Item(id: Int, name: String, description: String, user: Int, price: Double, deposit: Double,
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

	import ItemSystem._

	def createItem(i: ItemPostInfo): Item = {
		Item(99999, i.name, i.description, i.user, i.price, i.deposit, i.image, i.availableTime, i.transfer)
	}

	def patchItem(item: Item): Item = {
		item
	}

	def deleteItem(item: Item): Item = {
		item
	}

	override def receive: Receive = {
		case CreateItem(itemPostInfo) =>
			sender() ! createItem(itemPostInfo)
		case PatchItem(item) =>
			sender() ! patchItem(item)
		case DeleteItem(item) =>
			sender() ! deleteItem(item)
	}
}