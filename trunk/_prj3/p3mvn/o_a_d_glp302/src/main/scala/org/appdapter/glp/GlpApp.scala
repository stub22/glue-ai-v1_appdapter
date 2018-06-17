package org.appdapter.glp

import org.apache.jena.atlas.logging.LogCtl
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{path, _}
import akka.stream.ActorMaterializer

import scala.io.StdIn
/**
 * @author ${user.name}
 */
object GlpApp {
  val pathA = "patha"
  val pathB = "pathb"
  val pathF = "pathf"
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)
  
  def main(args : Array[String]) {
    println( "Hello World!" )
    println("concat arguments = " + foo(args))

    LogCtl.setLog4j

    import org.slf4j.LoggerFactory
    val logger = LoggerFactory.getLogger(classOf[App])
    logger.warn("logger warning whee")
    launchWebServer
  }
  def launchWebServer : Unit = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path(pathA) {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      } ~ // note tilde connects to next case
      path(pathB) {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say goodbye to akka-http</h1>"))
        }
      } ~ path(pathF) {
        val x = getSomeJsonLD()
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<pre>" + x + "</pre>"))
      }

      val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

      println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      bindingFuture
              .flatMap(_.unbind()) // trigger unbinding from the port
              .onComplete(_ => system.terminate()) // and shutdown when done
    }
  def getSomeJsonLD() : String = {
    val sds = new SomeDataStuff()
    val mdl = sds.loadThatModel()
    val mdmp = mdl.toString
    System.out.println("Loaded: " + mdmp)
    val jldTxt = sds.writeModelToJsonLDString_Pretty(mdl)
    System.out.println("Formatted: " + jldTxt)
    jldTxt
  }
}
