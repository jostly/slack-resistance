package slackres.playcontext.application

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import infrastructure.{SlackClient, BindActor, Resource}
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import parser._
import slackres.playcontext.command.CommandHandler
import slackres.playcontext.infrastructure.{SlackClientImpl, InMemoryGameStatusRepository, InMemoryDomainEventStore, DefaultRepository}
import slackres.playcontext.query.GameStatusTracker
import slackres.playcontext.resource._
import slackres.playcontext.saga.GameNotifier
import spray.can.Http
import spray.http.MediaTypes._
import spray.http.{FormData, StatusCodes}

import scala.concurrent.Await
import scala.concurrent.duration._

class PlayApplication(system: ActorSystem, port: Int = 8080, host: String = "localhost", slackUrl: String) {

  val domainEventStore = new InMemoryDomainEventStore

  val defaultRepository = new DefaultRepository(system.eventStream, domainEventStore)

  val commandHandler = new CommandHandler(defaultRepository)

  val gameStatusRepository = new InMemoryGameStatusRepository()

  val gameStatusTracker = system.actorOf(Props(classOf[GameStatusTracker], gameStatusRepository))

  val slackClient = new SlackClientImpl(slackUrl)

  val slackNotifier = system.actorOf(Props(classOf[GameNotifier], slackClient))

  val router = system.actorOf(Props(classOf[PlayRoutingActor],
    new CommandParser(commandHandler),
    new QueryParser(gameStatusRepository)), "play-routing")
  val binder = system.actorOf(Props(classOf[BindActor]), "play-binding")

  implicit val timeout = Timeout(5.seconds)

  def start() = {
    Await.result(binder ? Http.Bind(router, interface = host, port = port), 5.seconds)
  }

  def close(): Unit = {
    Await.result(binder ? Http.Unbind, 5.seconds)
  }

}

class PlayRoutingActor(commandParser: Parser, queryParser: Parser)
  extends Actor with Resource {

  implicit def json4sFormats: Formats = Serialization.formats(NoTypeHints)

  def actorRefFactory = context

  implicit val timeout: Timeout = 1.second

  val commandRoute =
    pathPrefix("form") {
      respondWithMediaType(`application/json`) {
        post {
          import spray.httpx.unmarshalling.FormDataUnmarshallers.UrlEncodedFormDataUnmarshaller
          entity(as[FormData]) { form =>
            val input = new SlackCommand(form.fields.toMap[String, String])
            detach() {
              complete {
                val parser = commandParser.parse(input).
                  orElse(queryParser.parse(input))

                parser.lift(input.text) match {
                  case Some(StatusCodeAndMessageResponse(code, message)) => code -> message
                  case Some(StatusCodeResponse(code)) => code
                  case None => StatusCodes.UnprocessableEntity
                }
              }
            }

          }
        }
      }
    }


  def receive = runRoute(
    pathPrefix("service") {
      commandRoute
    }
  )
}