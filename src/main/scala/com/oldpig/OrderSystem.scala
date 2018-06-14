package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

final case class OrderInfo(borrower: Int, lender: Int, item: Int, time: Int,
						   status: Int)

final case class OrderDeleteInfo(front_id: Int, user: Int, reason: String,
								 time: Int)

final case class OrderQueryInfo(user: Int, startTime: Int, endTime: Int,
								role: Int, state: Int)

final case class OrderQueryResult(front_id: Int, borrower: Int, lender: Int,
								  item: Int, start: Int, end: Int,
								  status: Int)

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

	lazy val dbSystem = context.actorSelection("../dbSystemActor")

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
		OrderQueryResults(List(OrderQueryResult(1, 1, 1, 1, 1, 1, 1),
			OrderQueryResult(1, 1, 1, 1, 1, 1, 1)))
	}

	def cancelOrder(orderDeleteInfo: OrderDeleteInfo): PatchResult = {
		PatchResult("succeed deleting order:" + orderDeleteInfo.front_id)
	}

	def createOrder(o: OrderInfo): PatchResult = {
		PatchResult("create order success, item:" + o.item)
	}

	def patchOrder(o: OrderInfo): PatchResult = {
		PatchResult("patch order succeed, item" + o.item)
	}
}
