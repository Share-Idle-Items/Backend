package com.oldpig

import akka.actor.{ Actor, ActorLogging, Props }

final case class ChatPostInfo(user1: String, user2: String)

final case class ChatList(list: List[ChatInfo])

final case class ChatInfo(front_id: String, user1: String, user2: String, lastMessage: Message)

final case class Message(sender: String, receiver: String, category: Int, content: String, time: Int)

final case class ChatRecords(records: List[Message])

object ChatSystem {
  def props = Props[ChatSystem]

  case class CreateChat(chatPostInfo: ChatPostInfo)

  case class GetChatList(user: Int)

  case class GetChatRecords(user: Int)

}

class ChatSystem extends Actor with ActorLogging {

  import ChatSystem._

  lazy val dbSystem = context.actorSelection("../dbSystemActor")

  override def receive: Receive = {
    case CreateChat(chatPostInfo) =>
      sender() ! createChat(chatPostInfo)
    case GetChatList(user) =>
      sender() ! getChatList(user)
    case GetChatRecords(user) =>
      sender() ! getChatRecords(user)
  }

  def createChat(chatPostInfo: ChatPostInfo): PatchResult = {
    PatchResult("succeed create chat: " + chatPostInfo.user1 + " " + chatPostInfo.user2)
  }

  def getChatList(user: Int): ChatList = {
    null
  }

  def getChatRecords(user: Int): ChatRecords = {
    null
  }
}
