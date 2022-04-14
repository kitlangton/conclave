package conclave.protocols

import zio._
import conclave.model
import conclave.model._
import conclave.protocol._
import conclave.repositories.{AccountRepository, EventRepository, QueryMuckery}

import java.time.Instant
import java.util.UUID

case class EventProtocolLive(
    eventRepository: EventRepository,
    accountRepository: AccountRepository,
    random: Random
) extends EventProtocol {
  override def allEvents: Task[List[Event]] =
    eventRepository.allEvents

  override def createEvent(newEvent: NewEvent): Task[Event] =
    eventRepository.save(
      model.Event(
        EventId(UUID.randomUUID()),
        newEvent.groupId,
        newEvent.title,
        newEvent.description,
        Instant.now()
      )
    )

  override def createRsvp(rsvp: Rsvp): Task[Unit] =
    eventRepository.createRsvp(rsvp)

  override def nextEvent: Task[Option[Event]] =
    eventRepository.nextEvent(QueryMuckery.gid)
}

object EventProtocolLive {
  val layer: URLayer[EventRepository with AccountRepository with Random, EventProtocol] =
    (EventProtocolLive.apply _).toLayer[EventProtocol]
}
