package client

//import scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import org.scalajs.dom
import rx._

import scalatags.JsDom.all._

import model.{Book, ChatMessage}
import client.utils.framework.Framework._
import org.scalajs.dom.html.Div

object ChatComponents {

  def buildChatContainer()(implicit ctx: Ctx.Owner): Div = {
    val userName = Var("")
    val message = Var("")
    val chatMessages = Var(Seq.empty[ChatMessage])

    var chatClient = new ChatClient((msg: ChatMessage) =>
      chatMessages() = chatMessages.now :+ msg
    )

    val userNameInput = input(
      `type` := "text",
      placeholder := "Type Username"
    ).render

    userNameInput.onkeyup = {
      (e: dom.Event) => {
        userName() = userNameInput.value
      }
    }

    val messageInput = input(
      `type` := "text",
      placeholder := "Type Message"
    ).render

    messageInput.onkeyup = {
      (e: dom.Event) => {
        message() = messageInput.value
      }
    }

    val sendButton = button(
      "send",
      onclick := { (e: dom.Event) =>
        val msg = ChatMessage(userName.now, message.now)
        chatClient.send(msg)
        message() = ""
        messageInput.value = ""
      }
    ).render

    val messageFragments = Rx {
      for(msg <- chatMessages())
        yield li(s"${msg.user}: ${msg.message}")
    }

    div(
      ul(
        messageFragments
      ),
      userNameInput,
      messageInput,
      sendButton
    ).render

  }
}
