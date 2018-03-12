package model.game

import java.util.UUID

import scala.collection.immutable.HashMap

object StageFactory {

  def createStage: Stage = {
    val stage = Stage(HashMap.empty, HashMap.empty)

    stage.addPlayer(
      Player(
        uuid = UUID.randomUUID().toString,
        x = 1,
        z = 1,
        rotY = 0f,
        l = 1,
        w = 1
      )
    )

    (for (x <- 1 to 21; z <- 1 to 21) yield (x, z)).foreach { case (x, z) =>
      (x, z) match {

        case (x, z) if (x > 1 && x < 21) && (z > 1 && z < 21) => {
          val indestructible = x % 2 == 0 && z % 2 == 0
          stage.addBlock(
            Block(
              uuid = UUID.randomUUID().toString,
              x = x,
              z = z,
              w = 1,
              l = 1,
              destructible = !indestructible
            )
          )
        }

        case (x, z) if (x == 1 || x == 21) && (2 < z && z < 20) => {
          stage.addBlock(
            Block(
              uuid = UUID.randomUUID().toString,
              x = x,
              z = z,
              w = 1,
              l = 1,
              destructible = true
            )
          )
        }

        case (x, z) if (z == 1 || z == 21) && (2 < x && x < 20) => {
          stage.addBlock(
            Block(
              uuid = UUID.randomUUID().toString,
              x = x,
              z = z,
              w = 1,
              l = 1,
              destructible = true
            )
          )
        }



        case _ => ()
      }
    }
    stage
  }
}
