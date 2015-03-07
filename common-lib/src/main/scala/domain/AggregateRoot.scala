package domain

import java.lang.reflect.Method

import event.DomainEvent

abstract class AggregateRoot[T <: GenericId] {

  var id: T = _
  var version: Int = 0
  var timestamp: Long = 0

  private var _uncommittedEvents: List[DomainEvent[T]] = Nil

  def nextVersion(): Int = version + 1

  def now(): Long = System.currentTimeMillis

  def loadFromHistory(history: List[DomainEvent[T]]) {
    for (event <- history) {
      applyChange(event, isNew = false)
    }
  }

  def applyVersionAndTimestamp(event: DomainEvent[T]) {
    this.version = event.version
    this.timestamp = event.timestamp
  }

  def applyChange(event: DomainEvent[T], isNew: Boolean = true) = {
    applyVersionAndTimestamp(event)
    invokeHandlerMethod(event)
    if (isNew) _uncommittedEvents ::= event
    this
  }

  private def invokeHandlerMethod(event: DomainEvent[T]) {
    try {
      val method: Method = getClass.getDeclaredMethod("handleEvent", event.getClass)
      method.setAccessible(true)
      method.invoke(this, event)
    }
    catch {
      case e: Exception =>
        throw new RuntimeException("Unable to call event handler method for " + event.getClass.getName, e)

    }
  }

  def hasUncommittedEvents: Boolean = {
    _uncommittedEvents.nonEmpty
  }

  def markChangesAsCommitted() {
    _uncommittedEvents = Nil
  }

  def uncommittedEvents() = _uncommittedEvents.reverse

  def ensure(condition: Boolean) {
    if (!condition) throw new IllegalStateException("requirement failed")
  }

  def ensure(condition: Boolean, message: => Any) {
    if (!condition) throw new IllegalStateException("requirement failed: " + message)
  }
}

