package conclave.protocol

import zio._
import zio.macros.accessible
import conclave.model._

@accessible
trait EventProtocol {
  def nextEvent: Task[Option[Event]]

  def allEvents: Task[List[Event]]

  def createEvent(newEvent: NewEvent): Task[Event]

  def createRsvp(rsvp: Rsvp): Task[Unit]
}
