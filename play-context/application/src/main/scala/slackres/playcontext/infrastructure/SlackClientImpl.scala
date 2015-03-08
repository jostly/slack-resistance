package slackres.playcontext.infrastructure

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.LazyLogging
import infrastructure.SlackClient
import org.json4s.{NoTypeHints, Formats}
import org.json4s.native.Serialization
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.httpx.Json4sSupport

import scala.concurrent.{Future, ExecutionContext}

class SlackClientImpl(url: String) extends SlackClient with Json4sSupport with LazyLogging {

  case class SlackPost(text: String, username: String, channel: String)

  implicit def json4sFormats: Formats = Serialization.formats(NoTypeHints)

  def post(channel: String, text: String, username: String = "ResistanceBot")(implicit factory: ActorRefFactory, executionContext: ExecutionContext): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    pipeline(Post(url, SlackPost(text, username, channel)))
  }

}

