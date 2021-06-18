package zymposium

import zio.random.Random
import zio.{Has, Task, UIO, URLayer}
import zio.stream.UStream
import zymposium.protocol._

import java.time.Instant
import java.util.UUID

case class EventServiceLive(
    eventRepository: EventRepository,
    accountRepository: AccountRepository,
    random: zio.random.Random.Service
) extends EventService {
  override def allEvents: Task[List[Event]] =
    eventRepository.allEvents

  override def createEvent(newEvent: NewEvent): Task[Event] =
    eventRepository.save(Event(UUID.randomUUID(), newEvent.title, newEvent.description, Instant.now()))

  override def allEventsStream: UStream[Event] = eventRepository.allEventsStream

  override def createAccount(newAccount: NewAccount): Task[Account] =
    accountRepository.save(Account(UUID.randomUUID(), newAccount.email))

  override def allAccountsStream: UStream[Account] =
    accountRepository.allAccountsStream

  override def createRsvp(rsvp: Rsvp): Task[Unit] =
    eventRepository.createRsvp(rsvp)

  override def rsvpStream: UStream[Rsvp] =
    eventRepository.rsvpStream
}

object EventServiceLive {
  val layer: URLayer[Has[EventRepository] with Has[AccountRepository] with Random, Has[EventService]] =
    (EventServiceLive.apply _).toLayer[EventService]
}
