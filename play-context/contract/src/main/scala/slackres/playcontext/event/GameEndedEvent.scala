package slackres.playcontext.event

import event.DomainEvent
import slackres.playcontext.domain.{User, GameId}

case class GameEndedEvent(aggregateId: GameId, version: Int, timestamp: Long)
  extends DomainEvent[GameId]
