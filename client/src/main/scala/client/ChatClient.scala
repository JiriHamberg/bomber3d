package client

import scala.scalajs.js.JSApp
import scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import org.scalajs.dom

import upickle.default._
import scala.collection.mutable.ArrayBuffer

import model.ChatMessage

class ChatClient(eventHandler: ChatMessage => Unit) {
  private val wsProto = if(dom.window.location.protocol == "https") "wss" else "ws"
  private val port = if(dom.window.location.port != "") s":${dom.window.location.port}" else ""
  private val serverName = s"${dom.window.location.hostname}${port}"
  private val path = s"${wsProto}://${serverName}/${model.Chat.ChatPrefix}/${model.Chat.ChatAtmospherePrefix}"
  private val socket = new dom.WebSocket(path)

  socket.onmessage = { (e: dom.MessageEvent) =>
    println(e)
    println(e.data)
    val chatMessage = read[ChatMessage](e.data.asInstanceOf[String])
    eventHandler(chatMessage)
  }

  socket.onopen = { (e: dom.Event) =>
    //println("Sending hello")
    //send(ChatMessage("me", "hello"))
  }

  def send(chatMessage: ChatMessage) = {
    socket.readyState match {
      case 1 => {
        socket.send(write(chatMessage))
      }
      case 0 => throw new IllegalStateException("Socket is connecting")
      case 2 => throw new IllegalStateException("Socket is closing")
      case 3 => throw new IllegalStateException("Socket is closed")
    }

  }

}
