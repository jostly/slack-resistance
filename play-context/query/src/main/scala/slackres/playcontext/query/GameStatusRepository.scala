package slackres.playcontext.query

import slackres.playcontext.domain.{GameStatus, GameId}

trait GameStatusRepository {

  def load(id: GameId): Option[GameStatus]

  def save(status: GameStatus)

}
