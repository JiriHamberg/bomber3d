package model.game

import scala.collection.immutable.HashMap

trait GameModel {
  val uuid: String
  def x: Float
  def z: Float
  def w: Float
  def l: Float
}


case class Block(
  uuid: String,
  x: Float,
  z: Float,
  w: Float,
  l: Float,
  destructible: Boolean
) extends GameModel

case class Player(
  uuid: String,
  var x: Float,
  var z: Float,
  var rotY: Float,
  l: Float,
  w: Float
) extends GameModel


case class Stage (
  var blocks: HashMap[String, Block],
  var players: HashMap[String, Player]
) {
  def addPlayer(player: Player): Unit = { players = players + Tuple2(player.uuid, player) }
  def addBlock(block: Block): Unit = { blocks = blocks + Tuple2(block.uuid, block) }
  def removeBlock(uuid: String): Unit = { blocks = blocks - uuid}
}


