package zymposium.repositories

import zio._
import zio.stream.{UStream, ZStream}
import zymposium.model.{Event, Rsvp}

import java.util.UUID

case class EventRepositoryTest(eventHub: Hub[Event], ref: Ref[Map[UUID, Event]]) extends EventRepository {
  override def allEvents: Task[List[Event]] =
    ref.get.map(_.values.toList)

  override def save(event: Event): Task[Event] =
    for {
      _ <- ref.update(_.updated(event.id, event))
      _ <- ref.get.map(_(event.id))
      _ <- eventHub.publish(event)
    } yield event

  override def allEventsStream: UStream[Event] = ZStream.fromHub(eventHub)

  // TODO: Implement.
  override def createRsvp(rsvp: Rsvp): Task[Unit]       = Task.unit
  override def rsvpStream: UStream[Rsvp]                = zio.stream.Stream.empty
  override def rsvps(accountId: UUID): Task[List[Rsvp]] = Task(List.empty)
  override def removeRsvp(rsvp: Rsvp): Task[Unit]       = Task.unit
}

object EventRepositoryTest {
  val layer: ULayer[Has[EventRepository]] = {
    for {
      ref <- Ref.make(Map.empty[UUID, Event])
      hub <- Hub.unbounded[Event]
    } yield EventRepositoryTest(hub, ref)
  }.toLayer
}
