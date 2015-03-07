package infrastructure

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.LazyLogging
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.httpx.Json4sSupport

import scala.concurrent.{ExecutionContext, Future}

class SlackClient(url: String) extends Json4sSupport with LazyLogging {

  case class SlackPost(text: String, username: String, channel: String)

  implicit def json4sFormats: Formats = Serialization.formats(NoTypeHints)

  def post(text: String, username: String = "ResistanceBot", channel: String = "#integration-test")(implicit factory: ActorRefFactory, executionContext: ExecutionContext): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    pipeline(Post(url, SlackPost(text, username, channel)))
  }

}
