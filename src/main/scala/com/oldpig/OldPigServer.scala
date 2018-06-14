package com.oldpig

//#quick-start-server
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

//#main-class
object OldPigServer extends App with OldPigRoutes {

	// set up ActorSystem and other dependencies here
	//#main-class
	//#server-bootstrapping
	implicit val system: ActorSystem = ActorSystem("oldPigServer")
	implicit val materializer: ActorMaterializer = ActorMaterializer()
	//#server-bootstrapping

//	val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

	val userSystem = system.actorOf(UserSystem.props, "userSystemActor")
	val itemSystem = system.actorOf(ItemSystem.props, "itemSystemActor")
	val orderSystem = system.actorOf(OrderSystem.props, "orderSystemActor")
	val fundSystem = system.actorOf(FundSystem.props, "fundSystemActor")
	val chatSystem = system.actorOf(ChatSystem.props, "chatSystemActor")
	val searchSystem = system.actorOf(SearchSystem.props, "searchSystemActor")
	//#main-class
	// from the UserRoutes trait
//	lazy val routes: Route = oldPigRoutes
	lazy val routes: Route = oldPigRoutes
	//#main-class

	//#http-server
	Http().bindAndHandle(routes, "localhost", 8080)

	println(s"Server online at http://localhost:8080/")

	Await.result(system.whenTerminated, Duration.Inf)
	//#http-server
	//#main-class
}
//#main-class
//#quick-start-server
