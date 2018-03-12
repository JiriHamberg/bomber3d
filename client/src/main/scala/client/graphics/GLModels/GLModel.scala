package client.graphics.GLModels


import org.scalajs.dom
import dom.raw._
import dom.raw.WebGLRenderingContext._
import scryetek.vecmath.Mat4

import scala.scalajs.js
import scala.scalajs.js.typedarray.{Float32Array, Uint8Array}


abstract class GLModel[T]()(implicit val gl: WebGLRenderingContext) {

  val fragmentShaderSource: String
  val vertexShaderSource: String

  implicit val matrixToFloatArray: (Mat4 => Float32Array) = { m: Mat4 =>
    new Float32Array(
      js.Array(
        m.m00, m.m10, m.m20, m.m30,
        m.m01, m.m11, m.m21, m.m31,
        m.m02, m.m12, m.m22, m.m32,
        m.m03, m.m13, m.m23, m.m33
      )
    )
  }

  var modelView: Mat4 = Mat4()
  lazy val program: WebGLProgram = initShaderProgram()
  lazy val modelBindings: T = bindModelData()

  def bindModelData(): T

  def draw(projectionMatrix: Mat4): Unit

  private def initShaderProgram(): WebGLProgram = {
    val vertexShader = loadShader(VERTEX_SHADER, vertexShaderSource)
    val fragmentShader = loadShader(FRAGMENT_SHADER, fragmentShaderSource)

    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertexShader)
    gl.attachShader(shaderProgram, fragmentShader)
    gl.linkProgram(shaderProgram)

    if (gl.getProgramParameter(shaderProgram, LINK_STATUS).asInstanceOf[Boolean] != true) {
      val errorMsg = gl.getProgramInfoLog(shaderProgram)
      throw new IllegalStateException(s"Unable to initialize the shader program: ${errorMsg}")
    }

    shaderProgram
  }

  private def loadShader(shaderType: Int, shaderSource: String): WebGLShader = {
    val shader = gl.createShader(shaderType)
    gl.shaderSource(shader, shaderSource)
    gl.compileShader(shader)

    if(gl.getShaderParameter(shader, COMPILE_STATUS).asInstanceOf[Boolean] != true) {
      val errorMsg = gl.getShaderInfoLog(shader)
      gl.deleteShader(shader)
      throw new IllegalStateException(s"Error occured while compiling shader: ${errorMsg}")
    }
    shader
  }

}

trait TexturedModel {

  implicit val gl: WebGLRenderingContext

  val textureUrls: Seq[String]

  //lazy val texture = loadTexture()

  lazy val textures = textureUrls map loadTexture

  private def loadTexture(textureUrl: String): WebGLTexture = {
    def isPowerOf2(i: Int): Boolean = {
      (i & -i) == i
    }

    val texture = gl.createTexture()
    gl.bindTexture(TEXTURE_2D, texture)

    val level = 0
    val internalFormat = RGBA
    val width = 1
    val height = 1
    val border = 0
    val srcFormat = RGBA
    val srcType = UNSIGNED_BYTE
    val pixel = new Uint8Array(js.Array(0, 0, 255, 255))
    gl.texImage2D(
      TEXTURE_2D,
      level,
      internalFormat,
      width,
      height,
      border,
      srcFormat,
      srcType,
      pixel
    )

    val image = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    image.onload = { ev: Event =>
      gl.bindTexture(TEXTURE_2D, texture)
      gl.texImage2D(
        TEXTURE_2D,
        level,
        internalFormat,
        srcFormat,
        srcType,
        image
      )
      //println("image width: " + image.width)
      //println("image height: " + image.height)

      if(isPowerOf2(image.width) && isPowerOf2(image.height)) {
        gl.generateMipmap(TEXTURE_2D)
      } else {
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
        gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
      }
    }
    image.src = textureUrl
    texture
  }
}