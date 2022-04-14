package conclave.protocol

import zio.UIO
import conclave.model.{EventId, Rsvp}

trait UserEventsProtocol {
  def rsvpedEvents: UIO[List[Rsvp]]

  def rsvp(event: EventId): UIO[Rsvp]
  def removeRsvp(event: EventId): UIO[Unit]
}
