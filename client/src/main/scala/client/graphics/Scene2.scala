package client.graphics

import client.graphics.GLModels.GLModelImpl._
import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import scryetek.vecmath.{Mat4, Vec3}

object Scene2 {

  def run()(implicit gl: WebGLRenderingContext) {

    var lastUpdated: Double = dom.window.performance.now()
    val projectionMatrix = getProjectionMatrix()

    val cube1 = new SimpleTexturedCube("assets/texture/brick_wall.png")
    val cube2 = new SimpleTexturedCube("assets/texture/brick.jpg")

    var cube1Rotation = 0f
    var cube2Rotation = 0f

    def draw(): Unit = {
      gl.clearColor(0.0, 0.0, 0.0, 1.0)
      gl.clearDepth(1.0)
      gl.enable(DEPTH_TEST)
      gl.depthFunc(LEQUAL)
      gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT)

      cube1.draw(projectionMatrix)
      cube2.draw(projectionMatrix)
    }


    def update(now: Double)(implicit gl: WebGLRenderingContext): Unit = {
      val dt = now - lastUpdated

      cube1Rotation += ( (dt / 1000) * 0.2 * Math.PI).toFloat
      cube2Rotation += ( (dt / 1000) * 0.1 * Math.PI).toFloat

      cube1.modelView = Mat4()
        .postTranslate(2f, 0f, 0f)
        .postRotate(cube1Rotation, Vec3(0, 0, 1))
        .postRotate(cube2Rotation, Vec3(0, 1, 0))
      cube2.modelView = Mat4()
        .postTranslate(-2f, 0f, 0f)
        .postRotate(cube2Rotation, Vec3(0, 0, 1))
        .postRotate(cube2Rotation, Vec3(0, 1, 0))

      lastUpdated = now
      draw()
      dom.window.requestAnimationFrame(update _)
    }

    dom.window.requestAnimationFrame(update _)
  }

  def getProjectionMatrix()(implicit gl: WebGLRenderingContext): Mat4 = {
    val fieldOfView: Float = (45 * Math.PI / 180).toFloat
    val aspect: Float = gl.canvas.clientWidth / gl.canvas.clientHeight
    val zNear = 0.1f
    val zFar = 100.0f
    Mat4.perspective(fieldOfView, aspect, zNear, zFar).postTranslate(0.0f, 0.0f, -10.0f)
  }


}
