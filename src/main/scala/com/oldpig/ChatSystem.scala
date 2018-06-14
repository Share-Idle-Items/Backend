package com.oldpig

import akka.actor.{Actor, ActorLogging, Props}
import com.oldpig.ChatSystem.{CreateChat, GetChatList, GetChatRecords}

final case class ChatPostInfo(user1: Int, user2: Int)

final case class ChatList(list: List[ChatInfo])

final case class ChatInfo(id: Int, user1: Int, user2: Int, lastMessage: Message)

final case class Message(sender: Int, content: String, time: Int)

final case class ChatRecords(records: List[Message])

object ChatSystem {
	def props = Props[ChatSystem]

	case class CreateChat(chatPostInfo: ChatPostInfo)

	case class GetChatList(user: Int)

	case class GetChatRecords(user: Int)

}

class ChatSystem extends Actor with ActorLogging {

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
		ChatList(List(ChatInfo(1, 1, 2, Message(1, "hi", 1)), ChatInfo(1, 2, 1, Message(2, "hi there", 3))))
	}

	def getChatRecords(user: Int): ChatRecords = {
		ChatRecords(List(Message(1, "hi", 1), Message(2, "hi there", 3)))
	}
}
