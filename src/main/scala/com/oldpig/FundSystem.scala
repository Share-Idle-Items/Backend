package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

final case class FundChangeInfo(user: Int, amount: Int, time: Int)

object FundSystem {
	def props = Props[FundSystem]

	case class FundDeposit(fundChangeInfo: FundChangeInfo)

	case class FundWithdraw(fundChangeInfo: FundChangeInfo)

}

class FundSystem extends Actor with ActorLogging {
	lazy val dbSystem = context.actorSelection("../dbSystemActor")

	import FundSystem._

	override def receive: Receive = {
		case FundDeposit(fundChangeInfo) =>
			sender() ! fundDeposit(fundChangeInfo)
		case FundWithdraw(fundChangeInfo) =>
			sender() ! fundWithdraw(fundChangeInfo)
	}

	def fundDeposit(fundChangeInfo: FundChangeInfo): PatchResult = {
		PatchResult("fund succeed: +" + fundChangeInfo.amount)
	}

	def fundWithdraw(fundChangeInfo: FundChangeInfo): PatchResult = {
		PatchResult("fund succeed: -" + fundChangeInfo.amount)
	}
}
