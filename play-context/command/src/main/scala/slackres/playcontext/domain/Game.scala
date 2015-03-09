package slackres.playcontext.domain

import domain.AggregateRoot
import slackres.playcontext.domain.Game._
import slackres.playcontext.event.{GameSettings, PlayerJoinedEvent, GameEndedEvent, GameCreatedEvent}

object Game {
  def create(id: GameId, creator: User, maxPlayers: Int) = {
    val game = new Game()
    game.create(id, creator, maxPlayers)
    game
  }
  
  sealed trait State
  case object Initializing extends State
  case object PlayersJoining extends State
  case object Playing extends State
  case object Ended extends State  
}

class Game extends AggregateRoot[GameId] {

  var state: State = Initializing
  var creator: User = _
  var players: List[User] = Nil
  var maxNumberOfPlayers: Int = _

  def create(id: GameId, creator: User, maxPlayers: Int) = {
    assertCanBeCreated()
    applyChange(GameCreatedEvent(id, nextVersion(), now(), creator, GameSettings(maxPlayers)))
    applyChange(PlayerJoinedEvent(id, nextVersion(), now(), creator))
  }

  def end(endedBy: User) = {
    assertCanBeEnded()
    applyChange(GameEndedEvent(id, nextVersion(), now(), Some(endedBy)))
  }

  def playerJoin(user: User) = {
    assertCanJoin(user)
    applyChange(PlayerJoinedEvent(id, nextVersion(), now(), user))
  }
  
  def assertCanBeCreated() = {
    ensure(state == Initializing || state == Ended)
  }

  def assertCanBeEnded() = {
    ensure(state != Ended)
  }

  def assertCanJoin(user: User) = {
    ensure(state == PlayersJoining)
    ensure(freeSeats >= 1)
    ensure(!players.contains(user))
  }

  def freeSeats: Int = maxNumberOfPlayers - players.length

  def handleEvent(event: GameCreatedEvent) = {
    this.id = event.aggregateId
    this.creator = event.createdBy
    this.maxNumberOfPlayers = event.settings.maxNumberOfPlayers
    this.players = Nil
    this.state = PlayersJoining
  }

  def handleEvent(event: GameEndedEvent) = {
    this.state = Ended
  }

  def handleEvent(event: PlayerJoinedEvent) = {
    players = event.player :: players
  }
}
