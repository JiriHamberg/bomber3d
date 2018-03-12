package model


case class ChatMessage(
  user: String,
  message: String
)

object Chat {
  val ChatPrefix = "chat"
  val ChatAtmospherePrefix = "the-chat"
}