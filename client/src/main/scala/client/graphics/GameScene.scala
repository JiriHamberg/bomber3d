package client.graphics

import org.scalajs.dom
import org.scalajs.dom.raw.{KeyboardEvent, WebGLRenderingContext}
import org.scalajs.dom.raw.WebGLRenderingContext._
import scryetek.vecmath.{Mat3, Mat4, Vec3}
import client.graphics.GLModels.GLModelImpl._
import model.game.StageFactory

case class PlayerState(
  var yRotation: Float,
  var position: Vec3
)


class GameScene(implicit gl: WebGLRenderingContext) {

  val stage = StageFactory.createStage
  var lastUpdated: Double = dom.window.performance.now()

  val playerState = PlayerState(
    stage.players.values.toSeq(0).rotY,
    Vec3(
      stage.players.values.toSeq(0).x,
      0f,
      stage.players.values.toSeq(0).z
    )
  )

  val skyboxModel = new Skybox(
    ("assets/texture/ely_cloudtop/cloudtop_up.png",
    "assets/texture/ely_cloudtop/cloudtop_dn.png",
    "assets/texture/ely_cloudtop/cloudtop_bk.png",
    "assets/texture/ely_cloudtop/cloudtop_ft.png",
    "assets/texture/ely_cloudtop/cloudtop_lf.png",
    "assets/texture/ely_cloudtop/cloudtop_rt.png"),
    450
  )
  val inDestructibleBrickModel = new SimpleTexturedCube("assets/texture/iron.jpg")
  val destructibleBrickModel = new SimpleTexturedCube("assets/texture/brick.jpg")
  val playerModel = new SimpleTexturedCube("assets/texture/grass.png")
  var projection = getProjectionFirstPerson()
  val modelView = Mat4.scale(0.5f, 0.5f, 0.5f)

  def run(): Unit = {
    //setUserInputHandlers()
    setUserInputHandlersFirstPerson()
    dom.window.requestAnimationFrame(update _)
  }

  private def update(now: Double)(implicit gl: WebGLRenderingContext): Unit = {
    val dt = now - lastUpdated

    lastUpdated = now
    draw()
    dom.window.requestAnimationFrame(update _)
  }

  private def getProjectionFirstPerson(): Mat4 = {
    val cameraOffset: Vec3 = Mat3.rotate(playerState.yRotation, 0f, 1f, 0f) * Vec3(0f, 3f, -7.5f)
    val centerOffset = Vec3(0f, 2f, 0f)

    val fieldOfView: Float = (45 * Math.PI / 180).toFloat
    val aspect: Float = gl.canvas.clientWidth / gl.canvas.clientHeight
    val zNear = 0.1f
    val zFar = 500.0f

    Mat4.perspective(fieldOfView, aspect, zNear, zFar) *
      Mat4.lookAt(playerState.position + cameraOffset, playerState.position + centerOffset, Vec3(0, 1, 0))
  }

  private def setUserInputHandlers(): Unit = {
    gl.canvas.onkeypress = { (event: KeyboardEvent) =>
      event.charCode match {
        case 'w' => stage.players.values.toSeq(0).z += 0.5f
        case 's' => stage.players.values.toSeq(0).z -= 0.5f
        case 'd' => stage.players.values.toSeq(0).x -= 0.5f
        case 'a' => stage.players.values.toSeq(0).x += 0.5f
      }
    }
  }

  private def setUserInputHandlersFirstPerson(): Unit = {
    gl.canvas.onkeypress = { (event: KeyboardEvent) =>
      event.charCode match {
        case 'w' => {
          val translation = Mat3.rotate(playerState.yRotation, Vec3(0f, 1f, 0f)) * Vec3(0f, 0f, 1f)
          playerState.position = playerState.position + translation

          stage.players.values.toSeq(0).x = playerState.position.x
          stage.players.values.toSeq(0).z = playerState.position.z
        }
        case 's' => {
          val translation = Mat3.rotate(playerState.yRotation, Vec3(0f, 1f, 0f)) * Vec3(0f, 0f, -1f)
          playerState.position = playerState.position + translation

          stage.players.values.toSeq(0).x = playerState.position.x
          stage.players.values.toSeq(0).z = playerState.position.z
        }
        case 'd' => {
          playerState.yRotation -= (5f * (Math.PI / 180f)).toFloat
          stage.players.values.toSeq(0).rotY = playerState.yRotation
        }
        case 'a' => {
          playerState.yRotation += (5f * (Math.PI / 180f)).toFloat
          stage.players.values.toSeq(0).rotY = playerState.yRotation
        }
      }
      projection = getProjectionFirstPerson()
    }
  }


  private def draw(): Unit = {
    gl.clearColor(0.0, 0.0, 0.0, 1.0)
    gl.clearDepth(1.0)
    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)
    gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT)


    skyboxModel.draw(projection)

    stage.blocks.foreach { case (_, block) =>
      val model = if(block.destructible) destructibleBrickModel else inDestructibleBrickModel
      model.modelView = Mat4.translate(block.x, 0f, block.z) * modelView
      model.draw(projection)
    }

    stage.players.foreach { case (_, player) =>
      val model = playerModel
      model.modelView = Mat4.translate(player.x, 0f, player.z) *
        Mat4.rotate(stage.players.values.toSeq(0).rotY, 0f, 1f, 0f) *
        modelView
      model.draw(projection)
    }
  }

  private def getProjectionMatrix()(implicit gl: WebGLRenderingContext): Mat4 = {
    val fieldOfView: Float = (45 * Math.PI / 180).toFloat
    val aspect: Float = gl.canvas.clientWidth / gl.canvas.clientHeight
    val zNear = 0.1f
    val zFar = 500.0f
    Mat4.perspective(fieldOfView, aspect, zNear, zFar) *
    Mat4.lookAt(Vec3(10.5f, 20f, -10.5f), Vec3(10.5f, 0f, 10.5f), Vec3(0, 1, 0))
  }

}
