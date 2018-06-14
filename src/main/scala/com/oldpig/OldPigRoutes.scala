package com.oldpig

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.oldpig.ChatSystem.{CreateChat, GetChatList, GetChatRecords}
import com.oldpig.FundSystem.{FundDeposit, FundWithdraw}
import com.oldpig.ItemSystem.{CreateItem, DeleteItem, PatchItem}
import com.oldpig.OrderSystem.{CancelOrder, CreateOrder, OrderQuery, PatchOrder}
import com.oldpig.SearchSystem.Search
import com.oldpig.UserSystem.{DeleteUser, GetUserInfo, Login, RegisterUser}

import scala.concurrent.Future
import scala.concurrent.duration._

final case class PatchResult(result: String)

//#user-routes-class
trait OldPigRoutes extends JsonSupport {
	//#user-routes-class

	// we leave these abstract, since they will be provided by the App
	implicit def system: ActorSystem

	lazy val log = Logging(system, classOf[OldPigRoutes])

	implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
	lazy val userRoutes =
		pathPrefix("user") {
			concat(
				pathEnd {
					concat(
						post {
							entity(as[RegisterInfo]) { r =>
								val userInfo: Future[UserInfo] = (userSystem ?
									RegisterUser(r)).mapTo[UserInfo]
								complete(userInfo)
							}
						},
						patch {
							entity(as[UserPatchInfo]) { p =>
								val patchResult: Future[PatchResult] =
									(userSystem ? p)
										.mapTo[PatchResult]
								complete(patchResult)
							}
						},
						delete {
							entity(as[Id]) { id =>
								val patchResult: Future[PatchResult] =
									(userSystem ? DeleteUser(id.id))
										.mapTo[PatchResult]
								complete(patchResult)
							}
						})
				},
				pathPrefix("session") {
					post {
						entity(as[LoginInfo]) { i =>
							val patchResult: Future[PatchResult] =
								(userSystem ? Login(i))
									.mapTo[PatchResult]
							complete(patchResult)
						}
					}
				},
				path(Segment) {
					st =>
						get {
							val userInfo: Future[UserInfo] = (userSystem ?
								GetUserInfo(st)).mapTo[UserInfo]
							complete(userInfo)
						}
//				}
				})
		}
	lazy val orderRoutes =
		pathPrefix("order") {
			concat(
				pathEnd {
					concat(
						post {
							entity(as[OrderInfo]) {
								o =>
									val patchResult = (orderSystem ? CreateOrder(o))
										.mapTo[PatchResult]
									complete(patchResult)
							}
						},
						patch {
							entity(as[OrderInfo]) {
								o =>
									val patchResult = (orderSystem ? PatchOrder(o))
										.mapTo[PatchResult]
									complete(patchResult)
							}
						},
						delete {
							entity(as[OrderDeleteInfo]) {
								o =>
									val patchResult = (orderSystem ? CancelOrder(o))
										.mapTo[PatchResult]
									complete(patchResult)
							}
						}
					)
				},
				pathPrefix("query") {
					post {
						entity(as[OrderQueryInfo]) {
							o =>
								val orderQueryResult = (orderSystem ? OrderQuery
								(o)).mapTo[OrderQueryResults]
								complete(orderQueryResult)
						}
					}
				}
			)
		}
	lazy val fundRoutes =
		pathPrefix("fund") {
			concat(
				pathPrefix("deposit") {
					post {
						entity(as[FundChangeInfo]) {
							i =>
								val patchResult = (fundSystem ? FundDeposit(i))
									.mapTo[PatchResult]
								complete(patchResult)
						}
					}
				},
				pathPrefix("withdrawal") {
					post {
						entity(as[FundChangeInfo]) {
							i =>
								val patchResult = (fundSystem ? FundWithdraw(i))
									.mapTo[PatchResult]
								complete(patchResult)
						}
					}
				}
			)
		}
	lazy val chatRoutes =
		pathPrefix("chat") {
			concat(
				post {
					entity(as[ChatPostInfo]) {
						i =>
							val patchResult = (chatSystem ? CreateChat(i)).mapTo[PatchResult]
							complete(patchResult)
					}
				},
				get {
					parameters('id.as[Int]) {
						id =>
							val chatList = (chatSystem ? GetChatList(id)).mapTo[ChatList]
							complete(chatList)
					}
				},
				path(Segment) {
					id =>
						get {
							val chatRecords = (chatSystem ? GetChatRecords(id.toInt)).mapTo[ChatRecords]
							complete(chatRecords)
						}
				}
			)
		}
	lazy val searchRoutes =
		pathPrefix("search") {
			post {
				entity(as[SearchInfo]) {
					i =>
						val searchResult = (searchSystem ? Search(i)).mapTo[ItemsList]
						complete(searchResult)
				}
			}
		}
	lazy val itemRoutes =
		pathPrefix("item") {
			concat(
				post {
					entity(as[ItemPostInfo]) {
						it =>
							val item = (itemSystem ? CreateItem(it)).mapTo[Item]
							complete(item)
					}
				},
				patch {
					entity(as[Item]) {
						it =>
							val item = (itemSystem ? PatchItem(it)).mapTo[Item]
							complete(item)
					}
				},
				delete {
					entity(as[Item]) {
						it =>
							val item = (itemSystem ? DeleteItem(it)).mapTo[Item]
							complete(item)
					}
				}
			)
		}

	lazy val oldPigRoutes: Route = concat(userRoutes, orderRoutes, fundRoutes, chatRoutes, searchRoutes, itemRoutes)
//		fundRoutes, chatRoutes, searchRoutes, itemRoutes, disputeRoutes)

	// other dependencies that UserRoutes use

	val userSystem, orderSystem, fundSystem, chatSystem, searchSystem, itemSystem: ActorRef

}
