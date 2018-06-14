package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}

final case class UserInfo(username: String, nickname: String, id: Int,
						  avatar: String)

final case class RegisterInfo(username: String, nickname: String,
							  password: String, email: String)

final case class Id(id: Int)

final case class UserPatchInfo(nickname: String, password: String,
							   email: String)

final case class LoginInfo(username: String, password: String)


object UserSystem {

	def props = Props[UserSystem]

	final case class RegisterUser(registerInfo: RegisterInfo)

	final case class GetUserInfo(id: Int)

	final case class DeleteUser(id: Int)

	final case class Login(loginInfo: LoginInfo)

}

class UserSystem extends Actor with ActorLogging {

	import UserSystem._

	override def receive: Receive = {
		case GetUserInfo(id) =>
			sender() ! getUserInfo(id)
		case RegisterUser(r) =>
			sender() ! registerUser(r)
		case UserPatchInfo(n, p, r) =>
			sender() ! userPatchInfo(n, p, r)
		case DeleteUser(id) =>
			sender() ! deleteUser(id)
		case Login(loginInfo) =>
			sender() ! login(loginInfo)
	}

	def login(loginInfo: LoginInfo): PatchResult = {
		PatchResult("cheat token")
	}

	def userPatchInfo(n: String, p: String, r: String): PatchResult = {
		PatchResult("patch succeed")
	}

	def deleteUser(id: Int): PatchResult = {
		PatchResult("delete succeed")
	}

	def getUserInfo(id: Any): UserInfo = {
		UserInfo("inf", "zq", 1, "noooo")
	}

	def registerUser(r: RegisterInfo): UserInfo = {
		UserInfo("reg", "zq", 1, "noooo")
	}

}
