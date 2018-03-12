package client.graphics.textures

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{Event, HTMLImageElement, WebGLRenderingContext, WebGLTexture}

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

object TextureFactory {

  def loadTextureCubeMap(urlUp: String, urlDn: String, urlBk: String, urlFt: String, urlLf: String, urlRt: String)(implicit gl: WebGLRenderingContext): WebGLTexture = {

    val texture = gl.createTexture()
    gl.bindTexture(TEXTURE_CUBE_MAP, texture)

    val textBindings = Seq(
      (urlUp, TEXTURE_CUBE_MAP_POSITIVE_Y),
      (urlDn, TEXTURE_CUBE_MAP_NEGATIVE_Y),
      (urlLf, TEXTURE_CUBE_MAP_NEGATIVE_X),
      (urlRt, TEXTURE_CUBE_MAP_POSITIVE_X),
      (urlFt, TEXTURE_CUBE_MAP_NEGATIVE_Z),
      (urlBk, TEXTURE_CUBE_MAP_POSITIVE_Z)
    )

    var loadCount = 0

    for((url, textureMapping) <- textBindings ) {
      val level = 0
      val internalFormat = RGBA
      val width = 1
      val height = 1
      val border = 0
      val srcFormat = RGBA
      val srcType = UNSIGNED_BYTE
      val pixel = new Uint8Array(js.Array(0, 0, 255, 255))

      gl.texImage2D(
        textureMapping,
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
        loadCount += 1

        gl.bindTexture(TEXTURE_CUBE_MAP, texture)
        gl.texImage2D(
          textureMapping,
          level,
          internalFormat,
          srcFormat,
          srcType,
          image
        )

        gl.texParameteri(TEXTURE_CUBE_MAP, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
        gl.texParameteri(TEXTURE_CUBE_MAP, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
        gl.texParameteri(TEXTURE_CUBE_MAP, TEXTURE_MIN_FILTER, LINEAR)

        if(loadCount == 6) {
          gl.generateMipmap(TEXTURE_CUBE_MAP)
        }

        //gl.texParameteri(TEXTURE_CUBE_MAP, TEXTURE_MIN_FILTER, LINEAR)
      }
      image.src = url
    }

    texture
  }


  def loadTexture2D(textureUrl: String)(implicit gl: WebGLRenderingContext): WebGLTexture = {

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

  private def isPowerOf2(i: Int): Boolean = {
    (i & -i) == i
  }


}
