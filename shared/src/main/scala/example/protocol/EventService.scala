package example.protocol

import boopickle.Default._
import zio._

import java.time.Instant
import java.util.{Date, UUID}

object CustomPicklers {
  implicit val datePickler: Pickler[Instant] =
    transformPickler((t: Long) => Instant.ofEpochMilli(t))(_.toEpochMilli)
}

case class Event(
    id: UUID,
    title: String,
    description: String,
    time: Long
    // Zoom
    // Creator account_id
)

case class NewEvent(title: String, description: String)

trait EventService {
  def magicNumber: UIO[Int]

  def allEvents: Task[List[Event]]

  def createEvent(newEvent: NewEvent): Task[Event]
}
