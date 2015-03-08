package slackres.playcontext.query

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import event.DomainEvent
import slackres.playcontext.domain.GameState._
import slackres.playcontext.domain.{GameId, GameStatus}
import slackres.playcontext.event.{GameCreatedEvent, GameEndedEvent, GameSettings, PlayerJoinedEvent}

class GameStatusTracker(repository: GameStatusRepository) extends Actor with LazyLogging {

  context.system.eventStream.subscribe(self, classOf[DomainEvent[_]])

  def receive = {
    case GameCreatedEvent(gameId, _, _, player, GameSettings(numberOfPlayers)) =>
      repository.save(GameStatus(gameId, numberOfPlayers, Set.empty, PlayersJoining))

    case GameEndedEvent(gameId, _, _, _) =>
      for (game <- repository.load(gameId); updatedGame <- Some(game.copy(state = Ended)))
        repository.save(updatedGame)

    case PlayerJoinedEvent(gameId, _, _, player) =>
      for (game <- repository.load(gameId); updatedGame <- Some(game.copy(players = game.players + player)))
        repository.save(updatedGame)

    case id: GameId => sender() ! repository.load(id)

  }

}
