package infrastructure

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import command.Command
import spray.httpx.Json4sSupport
import spray.routing.HttpService

trait Resource extends HttpService with Json4sSupport with LazyLogging {
  self: Actor =>

  def publish(message: AnyRef) = context.system.eventStream.publish(message)

}
