package client.graphics

import org.scalajs.dom
import dom.raw._
import dom.raw.WebGLRenderingContext._
import scryetek.vecmath.{Mat4, Vec3}

import scala.scalajs.js
import scala.scalajs.js.typedarray.{Float32Array, Uint16Array, Uint8Array}
import js.JSConverters._

object Scene {

  case class ProgramInfo(
    program: WebGLProgram,
    vertexPosition: Int,
    //vertexColor: Int,
    textureCoordinate: Int,
    vertexNormal: Int,
    projectionMatrix: WebGLUniformLocation,
    modelViewMatrix: WebGLUniformLocation,
    normalMatrix: WebGLUniformLocation,
    uniformSampler: WebGLUniformLocation,
    texture: WebGLTexture
  )

  case class BufferContainer(
    positionBuffer: WebGLBuffer,
    //colorBuffer: WebGLBuffer,
    textureCoordinateBuffer: WebGLBuffer,
    indexBuffer: WebGLBuffer,
    normalBuffer: WebGLBuffer
  )

  var squareRotation = 0.0f
  var lastUpdated: Double = dom.window.performance.now()


  val positions = new Float32Array(js.Array(
    // Front face
    -1.0, -1.0,  1.0,
    1.0, -1.0,  1.0,
    1.0,  1.0,  1.0,
    -1.0,  1.0,  1.0,

    // Back face
    -1.0, -1.0, -1.0,
    -1.0,  1.0, -1.0,
    1.0,  1.0, -1.0,
    1.0, -1.0, -1.0,

    // Top face
    -1.0,  1.0, -1.0,
    -1.0,  1.0,  1.0,
    1.0,  1.0,  1.0,
    1.0,  1.0, -1.0,

    // Bottom face
    -1.0, -1.0, -1.0,
    1.0, -1.0, -1.0,
    1.0, -1.0,  1.0,
    -1.0, -1.0,  1.0,

    // Right face
    1.0, -1.0, -1.0,
    1.0,  1.0, -1.0,
    1.0,  1.0,  1.0,
    1.0, -1.0,  1.0,

    // Left face
    -1.0, -1.0, -1.0,
    -1.0, -1.0,  1.0,
    -1.0,  1.0,  1.0,
    -1.0,  1.0, -1.0
  ))

  val vertexNormals = new Float32Array(
    js.Array(
      // Front
      0.0,  0.0,  1.0,
      0.0,  0.0,  1.0,
      0.0,  0.0,  1.0,
      0.0,  0.0,  1.0,

      // Back
      0.0,  0.0, -1.0,
      0.0,  0.0, -1.0,
      0.0,  0.0, -1.0,
      0.0,  0.0, -1.0,

      // Top
      0.0,  1.0,  0.0,
      0.0,  1.0,  0.0,
      0.0,  1.0,  0.0,
      0.0,  1.0,  0.0,

      // Bottom
      0.0, -1.0,  0.0,
      0.0, -1.0,  0.0,
      0.0, -1.0,  0.0,
      0.0, -1.0,  0.0,

      // Right
      1.0,  0.0,  0.0,
      1.0,  0.0,  0.0,
      1.0,  0.0,  0.0,
      1.0,  0.0,  0.0,

      // Left
      -1.0,  0.0,  0.0,
      -1.0,  0.0,  0.0,
      -1.0,  0.0,  0.0,
      -1.0,  0.0,  0.0
    )
  )

  val textureCoordinates = new Float32Array(
    js.Array(
      // Front
      0.0,  0.0,
      1.0,  0.0,
      1.0,  1.0,
      0.0,  1.0,
      // Back
      0.0,  0.0,
      1.0,  0.0,
      1.0,  1.0,
      0.0,  1.0,
      // Top
      0.0,  0.0,
      1.0,  0.0,
      1.0,  1.0,
      0.0,  1.0,
      // Bottom
      0.0,  0.0,
      1.0,  0.0,
      1.0,  1.0,
      0.0,  1.0,
      // Right
      0.0,  0.0,
      1.0,  0.0,
      1.0,  1.0,
      0.0,  1.0,
      // Left
      0.0,  0.0,
      1.0,  0.0,
      1.0,  1.0,
      0.0,  1.0
    )
  )

  /*val faceColors = Seq(
    Seq(1.0,  1.0,  1.0,  1.0),    // Front face: white
    Seq(1.0,  0.0,  0.0,  1.0),    // Back face: red
    Seq(0.0,  1.0,  0.0,  1.0),    // Top face: green
    Seq(0.0,  0.0,  1.0,  1.0),    // Bottom face: blue
    Seq(1.0,  1.0,  0.0,  1.0),    // Right face: yellow
    Seq(1.0,  0.0,  1.0,  1.0)     // Left face: purple
  )*/

  val vsSource =
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

  val fsSource =
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

  def initShaderProgram()(implicit gl: WebGLRenderingContext): WebGLProgram = {
    val vertexShader = loadShader(VERTEX_SHADER, vsSource)
    val fragmentShader = loadShader(FRAGMENT_SHADER, fsSource)

    val program = gl.createProgram()
    gl.attachShader(program, vertexShader)
    gl.attachShader(program, fragmentShader)
    gl.linkProgram(program)

    println("link status")
    println(gl.getProgramParameter(program, LINK_STATUS))

    if (gl.getProgramParameter(program, LINK_STATUS).asInstanceOf[Boolean] != true) {
      val errorMsg = gl.getProgramInfoLog(program)
      throw new IllegalStateException(s"Unable to initialize the shader program: ${errorMsg}")
    }

    program
  }

  def loadShader(shaderType: Int, shaderSource: String)(implicit gl: WebGLRenderingContext): WebGLShader = {
    val shader = gl.createShader(shaderType)
    gl.shaderSource(shader, shaderSource)
    gl.compileShader(shader)

    println("compile status")
    println(gl.getShaderParameter(shader, COMPILE_STATUS))

    if(gl.getShaderParameter(shader, COMPILE_STATUS).asInstanceOf[Boolean] != true) {
      val errorMsg = gl.getShaderInfoLog(shader)
      gl.deleteShader(shader)
      throw new IllegalStateException(s"Error occured while compiling shader: ${errorMsg}")
    }
    shader

  }


  def loadTexture(url: String)(implicit gl: WebGLRenderingContext): WebGLTexture = {

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
      println("image width: " + image.width)
      println("image height: " + image.height)

      if(isPowerOf2(image.width) && isPowerOf2(image.height)) {
        gl.generateMipmap(TEXTURE_2D)
      } else {
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
        gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
        gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
      }
    }
    image.src = url
    texture
  }

  def buildScene()(implicit gl: WebGLRenderingContext): ProgramInfo = {
    val shaderProgram = initShaderProgram()

    val texture = loadTexture("assets/texture/brick_wall.png")

    ProgramInfo(
      program = shaderProgram,
      vertexPosition = gl.getAttribLocation(shaderProgram, "aVertexPosition"),
      //vertexColor = gl.getAttribLocation(shaderProgram, "aVertexColor"),
      textureCoordinate = gl.getAttribLocation(shaderProgram, "aTextureCoord"),
      vertexNormal = gl.getAttribLocation(shaderProgram, "aVertexNormal"),
      projectionMatrix = gl.getUniformLocation(shaderProgram, "uProjectionMatrix"),
      normalMatrix = gl.getUniformLocation(shaderProgram, "uNormalMatrix"),
      modelViewMatrix = gl.getUniformLocation(shaderProgram, "uModelViewMatrix"),
      uniformSampler = gl.getUniformLocation(shaderProgram, "uSampler"),
      texture = texture
    )
  }

  def initBuffers()(implicit gl: WebGLRenderingContext): BufferContainer = {
    val positionBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, positionBuffer)
    gl.bufferData(ARRAY_BUFFER, positions, STATIC_DRAW)

    val textureCoordinateBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, textureCoordinateBuffer)
    gl.bufferData(ARRAY_BUFFER, textureCoordinates, STATIC_DRAW)

    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, indexBuffer)

    val indices = new Uint16Array(
      js.Array(
        0,  1,  2,      0,  2,  3,    // front
        4,  5,  6,      4,  6,  7,    // back
        8,  9,  10,     8,  10, 11,   // top
        12, 13, 14,     12, 14, 15,   // bottom
        16, 17, 18,     16, 18, 19,   // right
        20, 21, 22,     20, 22, 23   // left)
      )
    )
    gl.bufferData(ELEMENT_ARRAY_BUFFER, indices, STATIC_DRAW)

    val normalBuffer = gl.createBuffer()
    gl.bindBuffer(ARRAY_BUFFER, normalBuffer)
    gl.bufferData(ARRAY_BUFFER, vertexNormals, STATIC_DRAW)

    BufferContainer(
      positionBuffer = positionBuffer,
      textureCoordinateBuffer = textureCoordinateBuffer,
      indexBuffer = indexBuffer,
      normalBuffer = normalBuffer
    )

  }

  def drawScene(programInfo: ProgramInfo, buffers: BufferContainer)(implicit gl: WebGLRenderingContext): Unit = {

    def enableVertexData(): Unit = {
     val numComponents = 3
     val glType = FLOAT
     val normalize = false
     val stride = 0
     val bufferOffset = 0

     gl.bindBuffer(ARRAY_BUFFER, buffers.positionBuffer)
     gl.vertexAttribPointer(
       programInfo.vertexPosition,
       numComponents,
       glType,
       normalize,
       stride,
       bufferOffset
     )

     gl.enableVertexAttribArray(programInfo.vertexPosition)
   }

    /*def enableColorData(): Unit = {
      val numComponents = 4
      val glType = FLOAT
      val normalize = false
      val stride = 0    // how many bytes to get from one set of values to the next
      val bufferOffset = 0

      gl.bindBuffer(ARRAY_BUFFER, buffers.colorBuffer)
      gl.vertexAttribPointer(
        programInfo.vertexColor,
        numComponents,
        glType,
        normalize,
        stride,
        bufferOffset
      )
      gl.enableVertexAttribArray(programInfo.vertexColor)
    }*/

    def enableTextureData(): Unit = {
      val numComponents = 2
      val glType = FLOAT
      val normalize = false
      val stride = 0
      val offset = 0

      gl.bindBuffer(ARRAY_BUFFER, buffers.textureCoordinateBuffer)
      gl.vertexAttribPointer(
        programInfo.textureCoordinate,
        numComponents,
        glType,
        normalize,
        stride,
        offset
      )
      gl.enableVertexAttribArray(programInfo.textureCoordinate)
    }

    def enableNormalData(): Unit = {
      val numComponents = 3
      val glType = FLOAT
      val normalize = false
      val stride = 0
      val offset = 0
      gl.bindBuffer(ARRAY_BUFFER, buffers.normalBuffer)
      gl.vertexAttribPointer(
        programInfo.vertexNormal,
        numComponents,
        glType,
        normalize,
        stride,
        offset
      )
    }

    def drawElements(): Unit = {
      val vertexCount = 36
      val glType = UNSIGNED_SHORT
      val bufferOffset = 0
      gl.drawElements(TRIANGLES, vertexCount, glType, bufferOffset)
    }

    gl.clearColor(0.0, 0.0, 0.0, 1.0)
    gl.clearDepth(1.0)
    gl.enable(DEPTH_TEST)
    gl.depthFunc(LEQUAL)

    gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT)

    val fieldOfView: Float = (45 * Math.PI / 180).toFloat
    val aspect: Float = gl.canvas.clientWidth / gl.canvas.clientHeight
    val zNear = 0.1f
    val zFar = 100.0f

    val projectionMatrix = Mat4.perspective(fieldOfView, aspect, zNear, zFar)
    val modelViewMatrix = Mat4()
      .postTranslate(Vec3(0.0f, 0.0f, -6.0f))
      .postRotate(squareRotation, Vec3(0, 0, 1))
      .postRotate(squareRotation, Vec3(0, 1, 0))

    val normalMatrix = Mat4()
    modelViewMatrix.invert(out = normalMatrix)
    normalMatrix.transpose(out = normalMatrix)

    //modelViewMatrix.copy().invert().transpose()

    enableVertexData()
    //enableColorData()
    enableTextureData()
    enableNormalData()

    gl.bindBuffer(ELEMENT_ARRAY_BUFFER, buffers.indexBuffer)

    gl.useProgram(programInfo.program)

    gl.uniformMatrix4fv(
      programInfo.projectionMatrix,
      false,
      projectionMatrix
    )
    gl.uniformMatrix4fv(
      programInfo.modelViewMatrix,
      false,
      modelViewMatrix
    )
    gl.uniformMatrix4fv(
      programInfo.normalMatrix,
      false,
      normalMatrix
    )

    gl.activeTexture(TEXTURE0)
    gl.bindTexture(TEXTURE_2D, programInfo.texture)
    gl.uniform1i(programInfo.uniformSampler, 0)

    drawElements()

  }

}