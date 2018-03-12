package client.graphics.GLModels.GLModelImpl

import client.graphics.GLModels.{GLModel, TexturedModel}
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLUniformLocation}
import scryetek.vecmath.Mat4
import client.graphics.geometry.CubeGeometry
import client.graphics.textures.TextureFactory

case class SkyboxBindings(
  vertexPosition: Int,
  viewMatrix: WebGLUniformLocation,
  projectionMatrix: WebGLUniformLocation,
  cubeSampler: WebGLUniformLocation,
  positionBuffer: WebGLBuffer,
  indexBuffer: WebGLBuffer
)

class Skybox(val skyboxTextures: (String, String, String, String, String, String), depth: Float)
            (implicit gl: WebGLRenderingContext) extends GLModel[SkyboxBindings]()(gl) {

  modelView = Mat4.scale(depth / 2f, depth / 2f, depth / 2f)

  val texture = TextureFactory.loadTextureCubeMap(
    skyboxTextures._1,
    skyboxTextures._2,
    skyboxTextures._3,
    skyboxTextures._4,
    skyboxTextures._5,
    skyboxTextures._6
  )

  override val vertexShaderSource: String =
    """
      |attribute vec3 aVertexPosition;
      |
      |uniform mat4 uViewMatrix;
      |uniform mat4 uProjectionMatrix;
      |
      |varying highp vec3 vVertexPosition;
      |
      |void main() {
      |  gl_Position = uProjectionMatrix * uViewMatrix * vec4(aVertexPosition, 1.0);
      |  vVertexPosition = aVertexPosition;
      |}
    """.stripMargin

  override val fragmentShaderSource: String =
    """
      |//precision mediump float;
      |precision highp float;
      |uniform samplerCube uSampler;
      |varying vec3 vVertexPosition;
      |
      |void main(void) {
      |  gl_FragColor = textureCube(uSampler, vVertexPosition);
      |}
    """.stripMargin

  val positions = CubeGeometry.vertexPositions
//  val vertexNormals = CubeGeometry.vertexNormals
//  val textureCoordinates = CubeGeometry.textureCoordinates
  val vertexIndices = CubeGeometry.vertexIndices

  override def bindModelData(): SkyboxBindings = {

    val positionBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, positionBuffer)
    gl.bufferData(ARRAY_BUFFER, positions, STATIC_DRAW)

    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(ELEMENT_ARRAY_BUFFER, vertexIndices, STATIC_DRAW)

    SkyboxBindings(
      vertexPosition = gl.getAttribLocation(program, "aVertexPosition"),
      viewMatrix = gl.getUniformLocation(program, "uViewMatrix"),
      projectionMatrix = gl.getUniformLocation(program, "uProjectionMatrix"),
      cubeSampler = gl.getUniformLocation(program, "uSampler"),
      positionBuffer = positionBuffer,
      indexBuffer = indexBuffer
    )
  }

  override def draw(projectionMatrix: Mat4): Unit = {
    def enableVertexData(): Unit = {
      gl.bindBuffer(ARRAY_BUFFER, modelBindings.positionBuffer)
      gl.vertexAttribPointer(
        modelBindings.vertexPosition,
        3,
        FLOAT,
        false,
        0,
        0
      )
      gl.enableVertexAttribArray(modelBindings.vertexPosition)
    }

    def drawElements(): Unit = {
      val vertexCount = 36
      val glType = UNSIGNED_SHORT
      val bufferOffset = 0
      gl.drawElements(TRIANGLES, vertexCount, glType, bufferOffset)
    }

    enableVertexData()

    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, modelBindings.indexBuffer)

    gl.useProgram(program)

    gl.uniformMatrix4fv(
      modelBindings.projectionMatrix,
      false,
      projectionMatrix
    )
    gl.uniformMatrix4fv(
      modelBindings.viewMatrix,
      false,
      modelView
    )

    gl.activeTexture(TEXTURE0)
    gl.bindTexture(TEXTURE_CUBE_MAP, texture)
    gl.uniform1i(modelBindings.cubeSampler, 0)

    drawElements()
  }

}
