package com.oldpig

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }
import org.bson.types.ObjectId

final case class OrderInfo(front_id: String, borrower: String, lender: String, item: String, time: Int,
  status: Int)

final case class OrderDeleteInfo(front_id: String, user: String, reason: String,
  time: Int)

final case class OrderQueryInfo(user: String, startTime: Int, endTime: Int,
  role: Int, status: Int)

final case class OrderQueryResult(front_id: String, borrower: String, lender: String,
  item: String, time: Int, status: Int)

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

  implicit lazy val timeout = Timeout(5.seconds)
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

  def orderQuery(o: OrderQueryInfo): OrderQueryResults = {
    var query = $and("time" $gt o.startTime, "time" $lt o.endTime)
    if (o.status > 0) query = query ++ $and("status" -> o.status)
    query = o.role match {
      case 0 => query ++ $or("borrower" -> o.user, "lender" -> o.user)
      //			role: 1=borrower, 2=lender
      case 1 => query ++ $and("borrower" -> o.user)
      case 2 => query ++ $and("lender" -> o.user)
    }
    val f1 = (dbSystem ? DBSystem.Query("order", query)).mapTo[Array[DBObject]]
    val result = Await.result(f1, Duration.Inf)
    var ret = List[OrderQueryResult]()
    for (i <- result)
      ret ::= OrderQueryResult(
        i.get("_id").toString,
        i.get("borrower").toString,
        i.get("lender").toString,
        i.get("item").toString,
        i.get("time").toString.toInt,
        i.get("status").toString.toInt)
    OrderQueryResults(ret)
  }

  def cancelOrder(o: OrderDeleteInfo): PatchResult = {
    val query = MongoDBObject("_id" -> new ObjectId(o.front_id))
    val content = $set(
      "state" -> 10,
      "reason" -> o.reason,
      "deleteTime" -> o.time)
    val f1 = (dbSystem ? DBSystem.Update("order", query, content)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def createOrder(o: OrderInfo): PatchResult = {
    val content = MongoDBObject(
      "item" -> o.item,
      "borrower" -> o.borrower,
      "lender" -> o.lender,
      "status" -> o.status,
      "time" -> o.time)
    val f1 = (dbSystem ? DBSystem.Insert("order", content)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def patchOrder(o: OrderInfo): PatchResult = {
    val query = MongoDBObject("_id" -> new ObjectId(o.front_id))
    val content = $set(
      "item" -> o.item,
      "borrower" -> o.borrower,
      "lender" -> o.lender,
      "status" -> o.status)
    val f1 = (dbSystem ? DBSystem.Update("order", query, content)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }
}
