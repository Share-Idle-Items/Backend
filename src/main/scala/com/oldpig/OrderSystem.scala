package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}

final case class OrderInfo(borrower: Int, lender: Int, item: Int, start: Int,
						   end: Int, price: Double)

final case class OrderDeleteInfo(id: Int, user: Int, reason: String,
								 time: Int)

final case class OrderQueryInfo(user: Int, startTime: Int, endTime: Int,
								role: Int, state: Int)

final case class OrderQueryResult(id: Int, borrower: Int, lender: Int,
								  item: Int, start: Int, end: Int,
								  state: Int, price: Double)

final case class OrderQueryResults(list: List[OrderQueryResult])

object OrderSystem {
	def props = Props[OrderSystem]

	case class CreateOrder(orderInfo: OrderInfo)

	case class PatchOrder(orderInfo: OrderInfo)

	case class CancelOrder(orderDeleteInfo: OrderDeleteInfo)

	case class OrderQuery(orderQueryInfo: OrderQueryInfo)

}

class OrderSystem extends Actor with ActorLogging {

	import OrderSystem._

	override def receive: Receive = {
		case CreateOrder(o) =>
			sender() ! createOrder(o)
		case PatchOrder(o) =>
			sender() ! patchOrder(o)
		case CancelOrder(orderDeleteInfo) =>
			sender() ! cancelOrder(orderDeleteInfo)
		case OrderQuery(orderQueryInfo) =>
			sender() ! orderQuery(orderQueryInfo)
	}

	def orderQuery(orderQueryInfo: OrderQueryInfo): OrderQueryResults = {
		OrderQueryResults(List(OrderQueryResult(1, 1, 1, 1, 1, 1, 1, 2),
			OrderQueryResult(1, 1, 1, 1, 1, 1, 1, 2)))
	}

	def cancelOrder(orderDeleteInfo: OrderDeleteInfo): PatchResult = {
		PatchResult("succeed deleting order:" + orderDeleteInfo.id)
	}

	def createOrder(o: OrderInfo): PatchResult = {
		PatchResult("create order success, item:" + o.item)
	}

	def patchOrder(o: OrderInfo): PatchResult = {
		PatchResult("patch order succeed, item" + o.item)
	}
}
