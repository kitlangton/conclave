package zymposium.protocols

import zio.random.Random
import zio.stream.UStream
import zio.{Has, Task, URLayer}
import zymposium.model
import zymposium.model._
import zymposium.protocol._
import zymposium.repositories.{AccountRepository, EventRepository}

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
    eventRepository.save(model.Event(UUID.randomUUID(), newEvent.title, newEvent.description, Instant.now()))

  override def allEventsStream: UStream[Event] = eventRepository.allEventsStream

  override def createAccount(newAccount: NewAccount): Task[Account] =
    accountRepository.save(Account(UUID.randomUUID(), newAccount.email))

  override def allAccountsStream: UStream[Account] =
    accountRepository.allAccountsStream

  override def createRsvp(rsvp: Rsvp): Task[Unit] =
    eventRepository.createRsvp(rsvp)

  override def allRsvpsStream: UStream[Rsvp] =
    eventRepository.allRsvpsStream
}

object EventProtocolLive {
  val layer: URLayer[Has[EventRepository] with Has[AccountRepository] with Random, Has[EventProtocol]] =
    (EventProtocolLive.apply _).toLayer[EventProtocol]
}
