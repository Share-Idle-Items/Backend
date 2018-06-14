package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

final case class Location(province: String, city: String, region: String)

final case class SearchInfo(startTime: Int, endTime: Int, category: Int, lowPrice: Double, highPrice: Double,
							location: Location)

final case class ItemsList(list: List[Item])

object SearchSystem {
	def props = Props[SearchSystem]

	case class Search(searchInfo: SearchInfo)

}

class SearchSystem extends Actor with ActorLogging {
	lazy val dbSystem = context.actorSelection("../dbSystemActor")
	import SearchSystem._

	override def receive: Receive = {
		case Search(searchInfo) =>
			sender() ! search(searchInfo)
	}

	def search(searchInfo: SearchInfo): ItemsList = {
		ItemsList(List())
	}
}
