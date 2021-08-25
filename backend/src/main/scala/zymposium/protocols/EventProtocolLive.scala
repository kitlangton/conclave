package zymposium.protocols

import zio.random.Random
import zio.{Has, Task, URLayer}
import zymposium.model
import zymposium.model._
import zymposium.protocol._
import zymposium.repositories.{AccountRepository, EventRepository, QueryMuckery}

import java.time.Instant
import java.util.UUID

case class EventProtocolLive(
    eventRepository: EventRepository,
    accountRepository: AccountRepository,
    random: zio.random.Random.Service
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
  val layer: URLayer[Has[EventRepository] with Has[AccountRepository] with Random, Has[EventProtocol]] =
    (EventProtocolLive.apply _).toLayer[EventProtocol]
}
