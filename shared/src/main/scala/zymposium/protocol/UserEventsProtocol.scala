package zymposium.protocol

import zio.UIO
import zio.stream.UStream
import zymposium.model.Rsvp

import java.util.UUID

trait UserEventsProtocol {
//  def rsvpStream: UStream[Rsvp]
  def rsvpedEvents: UIO[List[Rsvp]]

  def rsvp(event: UUID): UIO[Rsvp]
  def removeRsvp(event: UUID): UIO[Unit]
}
