package com.oldpig

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//#main-class
object OldPigServer extends App with OldPigRoutes {

    // set up ActorSystem and other dependencies here
    //#server-bootstrapping
    implicit val system: ActorSystem = ActorSystem("oldPigServer")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val routes: Route = oldPigRoutes
    //	db connection
    //  create all subsystems
    val dbSystem = system.actorOf(DBSystem.props, "dbSystemActor")
    val userSystem = system.actorOf(UserSystem.props, "userSystemActor")
    val itemSystem = system.actorOf(ItemSystem.props, "itemSystemActor")
    val orderSystem = system.actorOf(OrderSystem.props, "orderSystemActor")
    val fundSystem = system.actorOf(FundSystem.props, "fundSystemActor")
    val chatSystem = system.actorOf(ChatSystem.props, "chatSystemActor")
    val searchSystem = system.actorOf(SearchSystem.props, "searchSystemActor")


    //#http-server
    Http().bindAndHandle(routes, "0.0.0.0", 8080)

    println(s"Server online at http://0.0.0.0:8080/")

    Await.result(system.whenTerminated, Duration.Inf)
}
