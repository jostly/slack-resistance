package slackres.playcontext.domain

import domain.AggregateRoot
import slackres.playcontext.domain.Game._
import slackres.playcontext.event.{GameEndedEvent, GameCreatedEvent}

object Game {
  def create(id: GameId, creator: User) = {
    val game = new Game()
    game.create(id, creator)
    game
  }
  
  sealed trait State
  case object Initializing extends State
  case object JoiningPlayers extends State
  case object Playing extends State
  case object Ended extends State  
}

class Game extends AggregateRoot[GameId] {

  var creator: User = _
  var state: State = Initializing

  def create(id: GameId, creator: User) = {
    assertCanBeCreated()
    applyChange(GameCreatedEvent(id, nextVersion(), now(), creator))
  }

  def end() = {
    assertCanBeEnded()
    applyChange(GameEndedEvent(id, nextVersion(), now()))
  }
  
  def assertCanBeCreated() = {
    assert(state == Initializing || state == Ended)
  }

  def assertCanBeEnded() = {
    assert(state != Ended)
  }

  def handleEvent(event: GameCreatedEvent) = {
    this.id = event.aggregateId
    this.version = event.version
    this.timestamp = event.timestamp
    this.creator = event.createdBy
    this.state = JoiningPlayers
  }

  def handleEvent(event: GameEndedEvent) = {
    this.version = event.version
    this.timestamp = event.timestamp
    this.state = Ended
  }
}
