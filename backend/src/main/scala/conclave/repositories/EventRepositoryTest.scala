package conclave.repositories

import zio._
import conclave.model._

case class EventRepositoryTest(eventHub: Hub[Event], ref: Ref[Map[EventId, Event]]) extends EventRepository {
  override def allEvents(groupId: GroupId): Task[List[Event]] =
    ref.get.map(_.values.toList)

  override def save(event: Event): Task[Event] =
    for {
      _ <- ref.update(_.updated(event.id, event))
      _ <- ref.get.map(_(event.id))
      _ <- eventHub.publish(event)
    } yield event

  override def createRsvp(rsvp: Rsvp): Task[Unit]               = Task.unit
  override def rsvps(accountId: AccountId): Task[List[Rsvp]]    = Task(List.empty)
  override def removeRsvp(rsvp: Rsvp): Task[Unit]               = Task.unit
  override def nextEvent(groupId: GroupId): Task[Option[Event]] = Task.none
}

object EventRepositoryTest {
  val layer: ULayer[EventRepository] = {
    for {
      ref <- Ref.make(Map.empty[EventId, Event])
      hub <- Hub.unbounded[Event]
    } yield EventRepositoryTest(hub, ref)
  }.toLayer
}
