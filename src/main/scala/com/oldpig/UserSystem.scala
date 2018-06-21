package com.oldpig

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

final case class UserInfo(username: String, real_name: String, front_id: String,
  phone: String, id_card: String, image: String, credit: Int)

final case class RegisterInfo(username: String, real_name: String, front_id: String,
  phone: String, id_card: String, image: String, credit: Int,
  password: String)

final case class Id(id: String)

final case class UserPatchInfo(front_id: String, phone: String, image: String, credit: Int,
  password: String)

final case class LoginInfo(username: String, password: String)

object UserSystem {

  def props = Props[UserSystem]

  final case class RegisterUser(registerInfo: RegisterInfo)

  final case class GetUserInfo(front_id: String)

  final case class DeleteUser(front_id: String)

  final case class Login(loginInfo: LoginInfo)

}

class UserSystem extends Actor with ActorLogging {
  lazy val dbSystem = context.actorSelection("../dbSystemActor")
  implicit lazy val timeout = Timeout(5.seconds)

  import UserSystem._

  override def receive: Receive = {
    case GetUserInfo(id) =>
      sender() ! getUserInfo(id)
    case RegisterUser(r) =>
      sender() ! registerUser(r)
    case UserPatchInfo(id, phone, image, credit, pw) =>
      sender() ! userPatchInfo(id, phone, image, credit, pw)
    case DeleteUser(id) =>
      sender() ! deleteUser(id)
    case Login(loginInfo) =>
      sender() ! login(loginInfo)
  }

  def login(loginInfo: LoginInfo): PatchResult = {
    val query = MongoDBObject(
      "username" -> loginInfo.username
    )
    val f1 = (dbSystem ? DBSystem.Query("user", query)).mapTo[Array[DBObject]]
    val result = Await.result(f1, Duration.Inf)
    if (result.isEmpty) PatchResult("Fail")
    else {
      if (result(0).get("password") == loginInfo.password) PatchResult("Success")
      else PatchResult("Fail")
    }
  }

  def userPatchInfo(id: String, phone: String, image: String, credit: Int, pw: String): PatchResult = {
    val query = MongoDBObject("front_id" -> id)
    val upd = $set(
      "phone" -> phone,
      "password" -> pw,
      "credit" -> credit,
      "image" -> image
    )
    val f1 = (dbSystem ? DBSystem.Update("user", query, upd)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def deleteUser(front_id: String): PatchResult = {
    val query = MongoDBObject("front_id" -> front_id)
    val f1 = (dbSystem ? DBSystem.Delete("user", query)).mapTo[String]
    PatchResult(Await.result(f1, Duration.Inf))
  }

  def getUserInfo(id: String): UserInfo = {
    val query = MongoDBObject("front_id" -> id)
    val f1 = (dbSystem ? DBSystem.Query("user", query)).mapTo[Array[DBObject]]
    val result = Await.result(f1, Duration.Inf)
    if (result.isEmpty)
      UserInfo("NotFound", "zq", "lala1", "18679991243", "320000000000000000", "no", 100)
    else {
      val t = result(0)
      UserInfo(t.get("username").toString, t.get("real_name").toString,
        t.get("front_id").toString, t.get("phone").toString,
        t.get("id_card").toString, t.get("image").toString, t.get("credit").toString.toInt)
    }
  }

  def registerUser(r: RegisterInfo): UserInfo = {
    val content = MongoDBObject(
      "username" -> r.username,
      "password" -> r.password,
      "real_name" -> r.real_name,
      "front_id" -> r.front_id,
      "phone" -> r.front_id,
      "id_card" -> r.id_card,
      "image" -> r.image,
      "credit" -> r.credit
    )
    val f1 = (dbSystem ? DBSystem.Insert("user", content)).mapTo[String]
    val fundContent = MongoDBObject("user" -> r.front_id, "balance" -> 0)
    val f2 = (dbSystem ? DBSystem.Insert("fund", fundContent)).mapTo[String]
    UserInfo(r.username, r.real_name, r.front_id, r.phone, r.id_card, r.image, r.credit)
  }

}
