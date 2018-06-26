package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.casbah.Imports._

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

final case class SearchInfo(user: String, name: String, startTime: Int, endTime: Int, category: String,
                            lowPrice: Double, highPrice: Double)

object SearchSystem {
    def props = Props[SearchSystem]

    case class Search(searchInfo: SearchInfo)

}

class SearchSystem extends Actor with ActorLogging {

    import SearchSystem._

    lazy val dbSystem = context.actorSelection("../dbSystemActor")
    implicit lazy val timeout = Timeout(5.seconds)

    override def receive: Receive = {
        case Search(searchInfo) =>
            sender() ! search(searchInfo)
    }

    def search(i: SearchInfo): ItemList = {
        var query = ("startTime" $gte i.startTime) ++ ("endTime" $lte i.endTime) ++
            ("price" $gte i.lowPrice $lte i.endTime)
        if (!i.category.isEmpty) query = query ++ ("category" $eq i.category)
        if (!i.user.isEmpty) query = query ++ ("user" $eq i.user)
        if (!i.name.isEmpty) query = query ++ ("name" $eq i.name)
        val f1 = (dbSystem ? DBSystem.Query("item", query)).mapTo[Array[DBObject]]
        val result = Await.result(f1, Duration.Inf)
        var ret = List[Item]()
        for (item <- result) {
            val locList = (List() ++ item("location").asInstanceOf[BasicDBList]) map {
                _.asInstanceOf[String]
            }
            ret ::= Item(
                item("front_id").toString,
                item.get("name").toString,
                item.get("description").toString,
                item.get("user").toString,
                item.get("price").toString.toDouble,
                item.get("deposit").toString.toDouble,
                (List() ++ item("image").asInstanceOf[BasicDBList]) map {
                    _.asInstanceOf[String]
                },
                //				item.as[List[String]]("image"),
                item.get("startTime").toString.toInt,
                item.get("endTime").toString.toInt,
                item.get("transfer").toString.toInt,
                //				item.as[Location]("location"),
                Location(locList(0), locList(1), locList(2)),
                item.get("category").toString,
                item.get("phone").toString.toInt
            )
        }
        ItemList(ret)
    }
}
