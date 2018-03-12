import org.scalatra.sbt._
import sbt.Project.projectToRef

//val Organization = "organization"
//val Name = "name"
//val Version = "0.0.1"

//lazy val ScalaVersion = "2.12.4"
lazy val ScalaVersion = "2.11.7"
lazy val ScalatraVersion = "2.6.+"

lazy val common = (crossProject.crossType(CrossType.Pure) in file("common"))
.settings(scalaVersion := ScalaVersion)

lazy val commonJvm = common.jvm
lazy val commonJs  = common.js

lazy val client = (project in file("client")).settings(
  scalaVersion := ScalaVersion,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.lihaoyi" %%% "scalatags" % "0.6.2",
    "com.lihaoyi" %%% "scalarx" % "0.3.2",
    //"be.doeraene" %%% "scalajs-jquery" % "0.9.1",
    "com.lihaoyi" %%% "upickle" % "0.4.4",
    "com.scryetek" %%% "vecmath" % "0.3.1"
    //"org.akka-js" %%% "akkajsactor" % "1.2.5.0",
    //"org.akka-js" %%% "akkajsactor" % "1.2.5.9",
    //"org.akka-js" %%% "akkajsactorstream" % "1.2.5.9",
    //"org.akka-js" %%% "akkajstestkit" % "1.2.5.0" % "test",
    //"org.akka-js" %%% "akkajstestkit" % "1.2.5.9" % "test",
    //"org.akka-js" %%% "akkajsstreamtestkit" % "1.2.5.9" % "test"
  ),
  scalaJSStage in Global := FastOptStage,
  skip in packageJSDependencies := false
)
.settings(
  Seq(fastOptJS, fullOptJS) map { packageJSKey =>
    crossTarget in (Compile, packageJSKey) := (baseDirectory in server).value / "src/main/webapp/assets/js"
  }
)
.enablePlugins(ScalaJSPlugin)
.dependsOn(commonJs)

lazy val server = (project in file("server"))
.settings(ScalatraPlugin.scalatraWithJRebel: _*)
.settings(
    sourcesInBase := false,
    //organization := Organization,
    //name := Name,
    //version := Version,
    scalaVersion := ScalaVersion,
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    scalacOptions := Seq("-deprecation", "-language:postfixOps"),
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
      "org.scalatra" %% "scalatra-json" % ScalatraVersion,
      "org.scalatra" %% "scalatra-atmosphere" % ScalatraVersion,
      //"com.typesafe.akka" %% "akka-http" % "10.0.5",
      "org.json4s" %% "json4s-jackson" % "3.5.0",
      "org.eclipse.jetty" % "jetty-webapp" % "9.3.9.v20160517" % "container;provided",
      "org.eclipse.jetty.websocket" %  "websocket-server"    % "9.3.9.v20160517"     % "container;provided",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test",
      "com.scryetek" %% "vecmath" % "0.3.1"
    ),
    javacOptions in compile ++= Seq("-target", "8", "-source", "8"),
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),
    packageOptions += Package.MainClass("JettyLauncher")
)
.enablePlugins(SbtTwirl, JettyPlugin)
.dependsOn(commonJvm)

addCommandAlias("compileAll", "; server/compile; client/fastOptJS")
