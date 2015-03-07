package infrastructure

import akka.actor.ActorRefFactory
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.httpx.Json4sSupport

import scala.concurrent.{ExecutionContext, Future}

object SlackClient extends Json4sSupport {

  case class SlackPost(text: String, username: String, channel: String)

  val url = ""

  implicit def json4sFormats: Formats = Serialization.formats(NoTypeHints)

  def post(text: String, username: String = "ResistanceBot", channel: String = "#integration-test")(implicit factory: ActorRefFactory, executionContext: ExecutionContext): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    pipeline(Post(url, SlackPost(text, username, channel)))
  }

}
