package slackres.playcontext.domain

case class GameStatus(id: GameId, maxNumberOfPlayers: Int, players: Set[User], state: GameState) {
  def freeSeats = maxNumberOfPlayers - players.size
}

sealed trait GameState

object GameState {

  case object PlayersJoining extends GameState
  case object Playing extends GameState
  case object Ended extends GameState

}


