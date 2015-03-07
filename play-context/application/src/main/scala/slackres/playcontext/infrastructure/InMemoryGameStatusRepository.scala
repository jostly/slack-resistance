package slackres.playcontext.infrastructure

import slackres.playcontext.domain.{GameStatus, GameId}
import slackres.playcontext.query.GameStatusRepository

class InMemoryGameStatusRepository extends GameStatusRepository {
  var map = Map.empty[GameId, GameStatus]

  override def load(id: GameId) = map.get(id)

  override def save(status: GameStatus) = map += (status.id -> status)

}
