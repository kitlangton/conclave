package example

import example.protocol.Event
import zio._

import java.util.UUID

case class EventRepositoryTest(ref: Ref[Map[UUID, Event]]) extends EventRepository {
  override def allEvents: Task[List[Event]] =
    ref.get.map(_.values.toList)

  override def save(event: Event): Task[Event] =
    ref.update(_.updated(event.id, event)) *> ref.get.map(_(event.id))
}

object EventRepositoryTest {
  val layer: ULayer[Has[EventRepository]] = {
    for {
      ref <- Ref.make(Map.empty[UUID, Event])
    } yield EventRepositoryTest(ref)
  }.toLayer
}
