package zymposium.repositories

import zio.macros.accessible
import zio.{query => _, _}
import zymposium.QuillContext
import zymposium.QuillContext._
import zymposium.model.{AccountId, Event, GroupId, Rsvp}

import java.sql.Connection
import java.util.UUID

@accessible
trait EventRepository {
  def nextEvent(groupId: GroupId): Task[Option[Event]]

  def save(event: Event): Task[Event]

  def allEvents: Task[List[Event]]

  def rsvps(accountId: AccountId): Task[List[Rsvp]]

  def createRsvp(rsvp: Rsvp): Task[Unit]

  def removeRsvp(rsvp: Rsvp): Task[Unit]
}

object EventRepository {
  val test: ULayer[Has[EventRepository]]                   = EventRepositoryTest.layer
  val live: URLayer[Has[Connection], Has[EventRepository]] = EventRepositoryLive.layer

  def save(event: Event): ZIO[Has[EventRepository], Throwable, Event] = ZIO.serviceWith[EventRepository](_.save(event))
}

case class EventRepositoryLive(
    newEventHub: Hub[Event],
    rsvpHub: Hub[Rsvp],
    connection: Connection
) extends EventRepository {

  lazy val env: Has[Connection] = Has(connection)

  // # EVENTS

  override def allEvents: Task[List[Event]] =
    run(query[Event]).provide(env)

  override def save(event: Event): Task[Event] =
    run(query[Event].insert(lift(event)).returningGenerated(_.id))
      .provide(env)
      .map(uuid => event.copy(id = uuid))
      .tap(newEventHub.publish)

  // # RSVPS

  override def createRsvp(rsvp: Rsvp): Task[Unit] =
    run(query[Rsvp].insert(lift(rsvp))).provide(env) *>
      rsvpHub.publish(rsvp).unit

  override def rsvps(accountId: AccountId): Task[List[Rsvp]] =
    run(query[Rsvp].filter(_.accountId == lift(accountId))).provide(env)

  override def removeRsvp(rsvp: Rsvp): Task[Unit] =
    run {
      query[Rsvp].filter(r => r.eventId == lift(rsvp.eventId) && r.accountId == lift(rsvp.accountId)).delete
    }
      .provide(env)
      .unit

  private def allRsvps: Task[List[Rsvp]] =
    run(query[Rsvp]).provide(env)

  override def nextEvent(groupId: GroupId): Task[Option[Event]] =
    run(
      query[Event]
        .filter(e => e.groupId == lift(groupId))
        .sortBy(_.time)
    )
      .map(_.headOption)
      .provide(env)
}

object QueryMuckery extends App {
  val gid = GroupId(UUID.fromString("c2aafb0a-7667-457f-a606-e6d73d2e91de"))

  import zio.magic._

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    EventRepository
      .nextEvent(gid)
      .debug("NEXT EVENT")
      .inject(QuillContext.live, EventRepositoryLive.layer)
      .exitCode
}

object EventRepositoryLive {
  val layer: URLayer[Has[Connection], Has[EventRepository]] = {
    for {
      connection  <- ZIO.service[Connection]
      newEventHub <- Hub.bounded[Event](256)
      rsvpHub     <- Hub.bounded[Rsvp](256)
    } yield EventRepositoryLive(newEventHub, rsvpHub, connection)
  }.toLayer
}
