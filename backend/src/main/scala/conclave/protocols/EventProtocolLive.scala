package conclave.protocols

import conclave.model
import conclave.model._
import conclave.protocol._
import conclave.repositories.{AccountRepository, EventRepository}
import zio._

import java.time.Instant
import java.util.UUID

case class EventProtocolLive(
    eventRepository: EventRepository,
    accountRepository: AccountRepository,
    random: Random
) extends EventProtocol {
  override def allEvents(groupId: GroupId): Task[List[Event]] = {
    println(s"allEvents(groupId = ${groupId})")
    eventRepository.allEvents(groupId)
  }

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
    eventRepository.nextEvent(GroupId(UUID.randomUUID()))
}

object EventProtocolLive {
  val layer: URLayer[EventRepository with AccountRepository with Random, EventProtocol] =
    (EventProtocolLive.apply _).toLayer[EventProtocol]
}

//object EventsStuff extends ZIOAppDefault {
//  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
//    EventProtocol
//      .createEvent(
//        NewEvent(
//          "Writing Tests with ZIO",
//          GroupId(UUID.fromString("40251d53-d50f-485e-baa7-0a2d6063deb0")),
//          "Learn how to use ZIO to write a simple application and test it."
//        )
//      )
//      .provideCustom(
//        EventProtocolLive.layer,
//        QuillContext.live,
//        AccountRepository.live,
//        PasswordHasher.live,
//        EventRepository.live
//      )
//}
