package slackres.playcontext.application

import akka.actor.ActorSystem

import scala.util.Properties

object Boot extends App {

  val slackUrl = Properties.envOrElse("TOKEN", "")

  new PlayApplication(
    system = ActorSystem("order-context"),
    port = Properties.envOrElse("PORT", "8080").toInt,
    host = Properties.envOrElse("HOST", "localhost"),
    slackUrl = s"https://hooks.slack.com/services/$slackUrl").start()

}
