package slackres.playcontext.saga

import akka.actor.Actor
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import event.DomainEvent
import infrastructure.SlackClient
import slackres.playcontext.domain.{GameId, User}
import slackres.playcontext.event.{GameCreatedEvent, GameEndedEvent, GameSettings, PlayerJoinedEvent}

import scala.concurrent.duration._

class GameNotifier(client: SlackClient) extends Actor with LazyLogging {

  context.system.eventStream.subscribe(self, classOf[DomainEvent[_]])

  import context.dispatcher

  implicit val timeout = Timeout(1.second)

  def receive = {
    case GameCreatedEvent(GameId(channelId), _, _, User(userName), GameSettings(numberOfPlayers)) =>
      client.post(channel = channelId,
        text = s"@$userName created a new game for up to $numberOfPlayers players. Playing games not yet implemented however.")

    case GameEndedEvent(GameId(channelId), _, _, endedBy) =>
      val user = endedBy.map(_.name).getOrElse("system")
      client.post(channel = channelId, text = s"Game was ended by $user.")

    case PlayerJoinedEvent(GameId(channelId), _, _, User(playerName)) =>
      client.post(channel = channelId, text = s"@$playerName joined the game.")
  }

}
