package infrastructure

import akka.actor.{Actor, ActorRef}
import akka.io.IO
import com.typesafe.scalalogging.LazyLogging
import spray.can.Http

/**
 * Binds an actor to a http port, keeping track of the control actor so
 * we can send an unbind message to it later
 */
class BindActor extends Actor with LazyLogging {

  def receive = {
    case bind: Http.Bind =>
      IO(Http)(context.system) ! bind
      context.become(binding(sender()))
  }

  def binding(app: ActorRef): Receive = {
    case b: Http.Bound =>
      app forward b
      context.become(bound(sender()))
    case x: AnyRef => logger.error(s"Binding received unexpected: $x")
  }

  def bound(control: ActorRef): Receive = {
    case unbind: Http.Unbind =>
      control.forward(unbind)
    case x: AnyRef => logger.error(s"Bound received unexpected: $x")
  }

}
