package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}

final case class FundChangeInfo(user: Int, amount: Int, time: Int)

object FundSystem {
	def props = Props[FundSystem]

	case class FundDeposit(fundChangeInfo: FundChangeInfo)

	case class FundWithdraw(fundChangeInfo: FundChangeInfo)

}

class FundSystem extends Actor with ActorLogging {

	import FundSystem._

	def fundDeposit(fundChangeInfo: FundChangeInfo): PatchResult = {
		PatchResult("fund succeed: +" + fundChangeInfo.amount)
	}

	def fundWithdraw(fundChangeInfo: FundChangeInfo): PatchResult = {
		PatchResult("fund succeed: -" + fundChangeInfo.amount)
	}

	override def receive: Receive = {
		case FundDeposit(fundChangeInfo) =>
			sender() ! fundDeposit(fundChangeInfo)
		case FundWithdraw(fundChangeInfo) =>
			sender() ! fundWithdraw(fundChangeInfo)
	}
}
