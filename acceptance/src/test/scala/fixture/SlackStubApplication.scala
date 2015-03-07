package fixture

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import infrastructure.BindActor
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import spray.can.Http
import spray.http.StatusCodes
import spray.routing.HttpService

import scala.concurrent.Await
import scala.concurrent.duration._

class SlackStubApplication(system: ActorSystem, port: Int = 8080, host: String = "localhost") {

  val router = system.actorOf(Props(classOf[SlackStubRoutingActor]), "stub-routing")
  val binder = system.actorOf(Props(classOf[BindActor]), "stub-binding")

  implicit val timeout = Timeout(5.seconds)

  def start() = {
    Await.result(binder ? Http.Bind(router, interface = host, port = port), 5.seconds)
  }

  def close(): Unit = {
    Await.result(binder ? Http.Unbind, 5.seconds)
  }
}

class SlackStubRoutingActor extends Actor with HttpService with LazyLogging {

  implicit def json4sFormats: Formats = Serialization.formats(NoTypeHints)

  def actorRefFactory = context

  implicit val timeout: Timeout = 1.second

  def receive = {
    runRoute(
      pathPrefix("services") {
        post {
          complete(StatusCodes.OK)
        }
      }
    )
  }
}
