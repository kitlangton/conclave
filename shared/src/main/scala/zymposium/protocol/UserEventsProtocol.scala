package zymposium.protocol

import zio.UIO
import zymposium.model.Rsvp

import java.util.UUID

trait UserEventsProtocol {
  def rsvpedEvents: UIO[List[Rsvp]]

  def rsvp(event: UUID): UIO[Rsvp]
  def removeRsvp(event: UUID): UIO[Unit]
}
