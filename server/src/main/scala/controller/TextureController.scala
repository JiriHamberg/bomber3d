package controller

import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.i18n.I18nSupport
import org.json4s.DefaultFormats

class TextureController extends ScalatraFilter with JacksonJsonSupport{

  implicit val jsonFormats = DefaultFormats

  get("/brick"){
    html.index()
  }

}
