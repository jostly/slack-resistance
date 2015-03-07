package slackres.playcontext.saga

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import event.DomainEvent
import infrastructure.SlackClient
import slackres.playcontext.domain.{User, GameId}
import slackres.playcontext.event.{GameEndedEvent, GameCreatedEvent}

class GameNotifier(client: SlackClient) extends Actor with LazyLogging {

  context.system.eventStream.subscribe(self, classOf[DomainEvent[_]])

  import context.dispatcher

  def receive = {
    case GameCreatedEvent(GameId(channelId), _, _, User(userName)) =>
      client.post(channel = channelId,
        text = s"@$userName created a new game. Joining and playing games not yet implemented.")

    case GameEndedEvent(GameId(channelId), _, _) =>
      client.post(channel = channelId, text = s"Game was ended by user.")
  }

}
