package client

import scala.scalajs.js.JSApp
import scala.scalajs.js
import scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import org.scalajs.dom
import dom.raw.WebGLRenderingContext
import scala.concurrent.duration._
import rx._
import scalatags.JsDom.all._
//import org.scalajs.jquery.jQuery
import dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global
import model.Book
import upickle.default._

import scala.concurrent.Future

import client.utils.framework.Framework._

//import client.ChatComponents

import client.graphics.{Scene, Scene2, GameScene}

@JSExportTopLevel("GameCanvas")
@JSExportAll
object GameCanvas {

  def main(container: dom.html.Canvas): Unit = {
    implicit val gl = container.getContext("webgl").asInstanceOf[WebGLRenderingContext]

    //val programInfo = Scene.buildScene()
    //val buffers = Scene.initBuffers()

    /*def updateScene(now: Double): Unit = {
      val dt = now - Scene.lastUpdated
      Scene.squareRotation = Scene.squareRotation + ( (dt / 1000) * 0.2 * Math.PI).toFloat
      Scene.lastUpdated = now
      Scene.drawScene(programInfo, buffers)
      dom.window.requestAnimationFrame(updateScene _)
    }

    dom.window.requestAnimationFrame(updateScene _ )
    */

    //Scene2.run()

    new GameScene().run()

  }


}
