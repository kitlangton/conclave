package zymposium.repositories

import zio.blocking.Blocking
import zio.macros.accessible
import zio.stream.{UStream, ZStream}
import zio.{query => _, _}
import zymposium.QuillContext._
import zymposium.model.{Event, Rsvp}

import java.sql.{Connection, Timestamp, Types}
import java.time.Instant
import java.util.UUID

@accessible
trait EventRepository {
  def removeRsvp(rsvp: Rsvp): Task[Unit]

  def allRsvpsStream: UStream[Rsvp]

  def allEvents: Task[List[Event]]
  def allEventsStream: UStream[Event]

  def allRsvps: Task[List[Rsvp]]
  def rsvps(accountId: UUID): Task[List[Rsvp]]
  def createRsvp(rsvp: Rsvp): Task[Unit]

  def save(event: Event): Task[Event]
}

object EventRepository {
  val test: ULayer[Has[EventRepository]]                                 = EventRepositoryTest.layer
  val live: URLayer[Has[Connection] with Blocking, Has[EventRepository]] = EventRepositoryLive.layer
}

case class EventRepositoryLive(
    newEventHub: Hub[Event],
    rsvpHub: Hub[Rsvp],
    connection: Connection,
    blocking: Blocking.Service
) extends EventRepository {

  implicit val instantEncoder: Encoder[Instant] =
    encoder(Types.TIMESTAMP, (index, value, row) => row.setTimestamp(index, Timestamp.from(value)))

  implicit val instantDecoder: Decoder[Instant] =
    decoder((index, row) => { row.getTimestamp(index).toInstant })

  lazy val env: Has[Connection] with Has[Blocking.Service] =
    Has(connection) ++ Has(blocking)

  override def allEvents: Task[List[Event]] =
    run(query[Event]).provide(env)

  override def save(event: Event): Task[Event] =
    for {
      event <- run { query[Event].insert(lift(event)).returningGenerated(_.id) }
        .provide(env)
        .map { uuid => event.copy(id = uuid) }
      _ <- newEventHub.publish(event)
    } yield event

  override def allEventsStream: UStream[Event] =
    ZStream.fromEffect(allEvents.orDie.map(Chunk.fromIterable(_))).flattenChunks ++
      ZStream.fromHub(newEventHub)

  override def createRsvp(rsvp: Rsvp): Task[Unit] =
    run(query[Rsvp].insert(lift(rsvp))).provide(env) *>
      rsvpHub.publish(rsvp).unit

  override def allRsvpsStream: UStream[Rsvp] =
    ZStream.fromEffect(allRsvps.orDie.map(Chunk.fromIterable)).flattenChunks ++
      ZStream.fromHub(rsvpHub)

  override def rsvps(accountId: UUID): Task[List[Rsvp]] =
    run(query[Rsvp].filter(_.accountId == lift(accountId))).provide(env)

  override def removeRsvp(rsvp: Rsvp): Task[Unit] =
    run {
      query[Rsvp].filter(r => r.eventId == lift(rsvp.eventId) && r.accountId == lift(rsvp.accountId)).delete
    }
      .provide(env)
      .unit

  override def allRsvps: Task[List[Rsvp]] =
    run(query[Rsvp]).provide(env)
}

object EventRepositoryLive {
  val layer: URLayer[Has[Connection] with Blocking, Has[EventRepository]] = {
    for {
      blocking    <- ZIO.service[Blocking.Service]
      connection  <- ZIO.service[Connection]
      newEventHub <- Hub.bounded[Event](256)
      rsvpHub     <- Hub.bounded[Rsvp](256)
    } yield EventRepositoryLive(newEventHub, rsvpHub, connection, blocking)
  }.toLayer
}
