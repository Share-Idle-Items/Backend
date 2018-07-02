package com.oldpig

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

final case class FundChangeInfo(user: String, amount: Int, time: Int)

object FundSystem {
  def props = Props[FundSystem]

  case class FundDeposit(fundChangeInfo: FundChangeInfo)

  case class FundWithdraw(fundChangeInfo: FundChangeInfo)

}

class FundSystem extends Actor with ActorLogging {

  import FundSystem._

  lazy val dbSystem = context.actorSelection("../dbSystemActor")
  implicit lazy val timeout = Timeout(5.seconds)

  override def receive: Receive = {
    case FundDeposit(fundChangeInfo) =>
      sender() ! fundDeposit(fundChangeInfo)
    case FundWithdraw(fundChangeInfo) =>
      sender() ! fundWithdraw(fundChangeInfo)
  }

  def fundDeposit(fundChangeInfo: FundChangeInfo): PatchResult = {
    val query = MongoDBObject("user" -> fundChangeInfo.user)
    val inc = $inc("balance" -> fundChangeInfo.amount)
    val content = MongoDBObject(
      "user" -> fundChangeInfo.user,
      "amount" -> fundChangeInfo.amount,
      "time" -> fundChangeInfo.time)
    val f1 = (dbSystem ? DBSystem.Update("fund", query, inc)).mapTo[String]
    dbSystem ? DBSystem.Insert("fundRecord", content)
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def fundWithdraw(fundChangeInfo: FundChangeInfo): PatchResult = {
    val query = MongoDBObject("user" -> fundChangeInfo.user)
    val dec = $inc("balance" -> -fundChangeInfo.amount)
    val content = MongoDBObject(
      "user" -> fundChangeInfo.user,
      "amount" -> -fundChangeInfo.amount,
      "time" -> fundChangeInfo.time)
    val f1 = (dbSystem ? DBSystem.Update("fund", query, dec)).mapTo[String]
    dbSystem ? DBSystem.Insert("fundRecord", content)
    PatchResult(Await.result(f1, Duration.Inf))
  }
}
