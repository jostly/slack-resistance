package slackres.playcontext.domain

import domain.AggregateRoot
import slackres.playcontext.domain.Game._
import slackres.playcontext.event.{PlayerJoinedEvent, GameEndedEvent, GameCreatedEvent}

object Game {
  def create(id: GameId, creator: User) = {
    val game = new Game()
    game.create(id, creator)
    game
  }
  
  sealed trait State
  case object Initializing extends State
  case object PlayersJoining extends State
  case object Playing extends State
  case object Ended extends State  
}

class Game extends AggregateRoot[GameId] {

  var creator: User = _
  var state: State = Initializing
  var players: List[User] = Nil

  def create(id: GameId, creator: User) = {
    assertCanBeCreated()
    applyChange(GameCreatedEvent(id, nextVersion(), now(), creator))
  }

  def end() = {
    assertCanBeEnded()
    applyChange(GameEndedEvent(id, nextVersion(), now()))
  }

  def addPlayer(user: User) = {
    assertCanJoin(user)
    applyChange(PlayerJoinedEvent(id, nextVersion(), now(), user))
  }
  
  def assertCanBeCreated() = {
    assert(state == Initializing || state == Ended)
  }

  def assertCanBeEnded() = {
    assert(state != Ended)
  }

  def assertCanJoin(user: User) = {
    assert(state == PlayersJoining)
    assert(freeSeats >= 1)
    assert(!players.contains(user))
  }

  def freeSeats: Int = 10 - players.length

  def handleEvent(event: GameCreatedEvent) = {
    this.id = event.aggregateId
    this.creator = event.createdBy
    this.state = PlayersJoining
  }

  def handleEvent(event: GameEndedEvent) = {
    this.state = Ended
  }

  def handleEvent(event: PlayerJoinedEvent) = {
    players = event.player :: players
  }
}
