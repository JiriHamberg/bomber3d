package client.graphics.GLModels.GLModelImpl

import client.graphics.GLModels.{GLModel, TexturedModel}
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLUniformLocation}
import scryetek.vecmath.Mat4

import client.graphics.geometry.CubeGeometry

case class TexturedCubeProgramBindings(
  programInfo: TexturedCubeProgramInfo,
  programData: TexturedCubeProgramData
)

case class TexturedCubeProgramInfo(
  vertexPosition: Int,
  textureCoordinate: Int,
  vertexNormal: Int,
  projectionMatrix: WebGLUniformLocation,
  modelViewMatrix: WebGLUniformLocation,
  normalMatrix: WebGLUniformLocation,
  uniformSampler: WebGLUniformLocation
)

case class TexturedCubeProgramData(
  positionBuffer: WebGLBuffer,
  textureCoordinateBuffer: WebGLBuffer,
  indexBuffer: WebGLBuffer,
  normalBuffer: WebGLBuffer
)

class SimpleTexturedCube(val textureUrl: String)(implicit gl: WebGLRenderingContext) extends GLModel[TexturedCubeProgramBindings]()(gl) with TexturedModel {
  override val textureUrls = Seq(textureUrl)

  override val fragmentShaderSource: String =
    """
      |varying highp vec2 vTextureCoord;
      |varying highp vec3 vLighting;
      |
      |uniform sampler2D uSampler;
      |
      |void main(void) {
      |  highp vec4 texelColor = texture2D(uSampler, vTextureCoord);
      |
      |  gl_FragColor = vec4(texelColor.rgb * vLighting, texelColor.a);
      |}
    """.stripMargin

  override val vertexShaderSource: String =
    """
      |attribute vec4 aVertexPosition;
      |attribute vec3 aVertexNormal;
      |attribute vec2 aTextureCoord;
      |
      |uniform mat4 uNormalMatrix;
      |uniform mat4 uModelViewMatrix;
      |uniform mat4 uProjectionMatrix;
      |
      |varying highp vec2 vTextureCoord;
      |varying highp vec3 vLighting;
      |
      |void main() {
      |  gl_Position = uProjectionMatrix * uModelViewMatrix * aVertexPosition;
      |  vTextureCoord = aTextureCoord;
      |
      |  // Apply lighting effect
      |
      |  highp vec3 ambientLight = vec3(0.3, 0.3, 0.3);
      |  highp vec3 directionalLightColor = vec3(1, 1, 1);
      |  highp vec3 directionalVector = normalize(vec3(0.85, 0.8, 0.75));
      |
      |  highp vec4 transformedNormal = uNormalMatrix * vec4(aVertexNormal, 1.0);
      |
      |  highp float directional = max(dot(transformedNormal.xyz, directionalVector), 0.0);
      |  vLighting = ambientLight + (directionalLightColor * directional);
      |}
    """.stripMargin

  val positions = CubeGeometry.vertexPositions
  val vertexNormals = CubeGeometry.vertexNormals
  val textureCoordinates = CubeGeometry.textureCoordinates
  val vertexIndices = CubeGeometry.vertexIndices

  override def bindModelData(): TexturedCubeProgramBindings = {
    val programInfo = TexturedCubeProgramInfo(
      vertexPosition = gl.getAttribLocation(program, "aVertexPosition"),
      textureCoordinate = gl.getAttribLocation(program, "aTextureCoord"),
      vertexNormal = gl.getAttribLocation(program, "aVertexNormal"),
      projectionMatrix = gl.getUniformLocation(program, "uProjectionMatrix"),
      normalMatrix = gl.getUniformLocation(program, "uNormalMatrix"),
      modelViewMatrix = gl.getUniformLocation(program, "uModelViewMatrix"),
      uniformSampler = gl.getUniformLocation(program, "uSampler")
    )

    val positionBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, positionBuffer)
    gl.bufferData(ARRAY_BUFFER, positions, STATIC_DRAW)

    val textureCoordinateBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, textureCoordinateBuffer)
    gl.bufferData(ARRAY_BUFFER, textureCoordinates, STATIC_DRAW)

    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(ELEMENT_ARRAY_BUFFER, vertexIndices, STATIC_DRAW)

    val normalBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, normalBuffer)
    gl.bufferData(ARRAY_BUFFER, vertexNormals, STATIC_DRAW)

    val programData = TexturedCubeProgramData(
      positionBuffer = positionBuffer,
      textureCoordinateBuffer = textureCoordinateBuffer,
      indexBuffer = indexBuffer,
      normalBuffer = normalBuffer
    )

    TexturedCubeProgramBindings(
      programInfo,
      programData
    )
  }

  override def draw(projectionMatrix: Mat4): Unit = {
    def enableVertexData(): Unit = {
      gl.bindBuffer(ARRAY_BUFFER, modelBindings.programData.positionBuffer)
      gl.vertexAttribPointer(
        modelBindings.programInfo.vertexPosition,
        3,
        FLOAT,
        false,
        0,
        0
      )
      gl.enableVertexAttribArray(modelBindings.programInfo.vertexPosition)
    }

    def enableTextureData(): Unit = {
      gl.bindBuffer(ARRAY_BUFFER, modelBindings.programData.textureCoordinateBuffer)
      gl.vertexAttribPointer(
        modelBindings.programInfo.textureCoordinate,
        2,
        FLOAT,
        false,
        0,
        0
      )
      gl.enableVertexAttribArray(modelBindings.programInfo.textureCoordinate)
    }

    def enableNormalData(): Unit = {
      gl.bindBuffer(ARRAY_BUFFER, modelBindings.programData.normalBuffer)
      gl.vertexAttribPointer(
        modelBindings.programInfo.vertexNormal,
        3,
        FLOAT,
        false,
        0,
        0
      )
    }

    def drawElements(): Unit = {
      val vertexCount = 36
      val glType = UNSIGNED_SHORT
      val bufferOffset = 0
      gl.drawElements(TRIANGLES, vertexCount, glType, bufferOffset)
    }

    val normalMatrix = Mat4()
    modelView.invert(out = normalMatrix)
    normalMatrix.transpose(out = normalMatrix)

    enableVertexData()
    enableTextureData()
    enableNormalData()

    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, modelBindings.programData.indexBuffer)

    gl.useProgram(program)

    gl.uniformMatrix4fv(
      modelBindings.programInfo.projectionMatrix,
      false,
      projectionMatrix
    )
    gl.uniformMatrix4fv(
      modelBindings.programInfo.modelViewMatrix,
      false,
      modelView
    )
    gl.uniformMatrix4fv(
      modelBindings.programInfo.normalMatrix,
      false,
      normalMatrix
    )

    gl.activeTexture(TEXTURE0)
    gl.bindTexture(TEXTURE_2D, textures(0))
    gl.uniform1i(modelBindings.programInfo.uniformSampler, 0)

    drawElements()
  }

}
