package slackres.playcontext.event

import event.DomainEvent
import slackres.playcontext.domain.{User, GameId}

case class GameCreatedEvent(aggregateId: GameId, version: Int, timestamp: Long, createdBy: User, settings: GameSettings)
  extends DomainEvent[GameId]

case class GameSettings(maxNumberOfPlayers: Int)
