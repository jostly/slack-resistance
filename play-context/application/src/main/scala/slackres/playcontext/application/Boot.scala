package slackres.playcontext.application

import akka.actor.ActorSystem

import scala.util.Properties

object Boot extends App {

  new PlayApplication(
    system = ActorSystem("order-context"),
    port = Properties.envOrElse("PORT", "8080").toInt,
    host = Properties.envOrElse("HOST", "localhost"),
    slackUrl = Properties.envOrElse("SLACK_WEBHOOKS_URL", "unconfigured")).start()

}
