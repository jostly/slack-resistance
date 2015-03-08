package infrastructure

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.LazyLogging
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.httpx.Json4sSupport

import scala.concurrent.{ExecutionContext, Future}

trait SlackClient {
  def post(channel: String, text: String, username: String = "ResistanceBot")(implicit factory: ActorRefFactory, executionContext: ExecutionContext): Future[HttpResponse]
}