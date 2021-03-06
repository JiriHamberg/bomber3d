package controller

import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.i18n.I18nSupport
import org.json4s.DefaultFormats
import model.Book

class IndexController extends ScalatraFilter with JacksonJsonSupport{

  implicit val jsonFormats = DefaultFormats

  get("/"){
    html.index()
  }

  post("/books"){
    contentType = formats("json")
    Seq(
      Book("Scalatra in Action", Seq("Dave Hrycyszyn", "Stefan Ollinger", "Ross A. Baker")),
      Book("Scala Recipes", Seq("Naoki Takezoe", "Takako Shimamoto")),
      Book("Fake Book Title", Seq("Author Number One", "Author Number Two"))
    )
  }

}
