package controller

import model.ChatMessage
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.scalatra._
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}

import scala.concurrent.ExecutionContext.Implicits.global

class ChatController extends ScalatraServlet
  with AtmosphereSupport
  with JValueResult
  with JacksonJsonSupport
  with SessionSupport  {

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere(s"/${model.Chat.ChatAtmospherePrefix}") {
    new AtmosphereClient {
      def receive: AtmoReceive = {
        case Connected =>
          println("Client %s is connected" format uuid)
          //broadcast(("author" -> "Someone") ~ ("message" -> "joined the room"), Everyone)

        case Disconnected(ClientDisconnected, _) =>
          println("Client %s has disconnected" format uuid)
          //broadcast(("author" -> "Someone") ~ ("message" -> "has left the room"), Everyone)

        case Disconnected(ServerDisconnected, _) =>
          println("Server disconnected the client %s" format uuid)
        case TextMessage(msg) =>
          //send(("author" -> "system") ~ ("message" -> "Only json is allowed"))
          println(s"got msg $msg")
          val chatMessage: ChatMessage =  parse(msg).extract[ChatMessage]
          broadcast(write(chatMessage), Everyone)

        case JsonMessage(json) =>
          val chatMessage = json.extract[ChatMessage]
          println("Got message %s from %s".format(chatMessage.message, chatMessage.user))
          broadcast(write(chatMessage), Everyone) // by default a broadcast is to everyone but self
        //  send(msg) // also send to the sender
      }
    }
  }

}

