package slackres.playcontext.saga

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import event.DomainEvent
import infrastructure.SlackClient
import slackres.playcontext.domain.{GameId, GameStatus, User}
import slackres.playcontext.event.{GameCreatedEvent, GameEndedEvent, GameSettings, PlayerJoinedEvent}

import scala.util.Success

import scala.concurrent.duration._

class GameNotifier(client: SlackClient, statusTracker: ActorRef) extends Actor with LazyLogging {

  context.system.eventStream.subscribe(self, classOf[DomainEvent[_]])

  import context.dispatcher

  implicit val timeout = Timeout(1.second)

  def receive = {
    case GameCreatedEvent(GameId(channelId), _, _, User(userName), GameSettings(numberOfPlayers)) =>
      client.post(channel = channelId,
        text = s"@$userName created a new game for up to $numberOfPlayers players. Playing games not yet implemented however.")

    case GameEndedEvent(GameId(channelId), _, _) =>
      client.post(channel = channelId, text = s"Game was ended by user.")

    case PlayerJoinedEvent(GameId(channelId), _, _, User(playerName)) =>
      (statusTracker ? GameId(channelId)).onComplete {
        case Success(status: GameStatus) => client.post(channel = channelId, text = s"@$playerName joined the game, leaving room for ${status.freeSeats} more players.")
        case _ => client.post(channel = channelId, text = s"@$playerName joined the game.")
      }
  }

}
