package zymposium.protocol

import zio.UIO
import zymposium.model.{EventId, Rsvp}

trait UserEventsProtocol {
  def rsvpedEvents: UIO[List[Rsvp]]

  def rsvp(event: EventId): UIO[Rsvp]
  def removeRsvp(event: EventId): UIO[Unit]
}
