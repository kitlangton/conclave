package conclave.repositories

import zio.macros.accessible
import zio.{query => _, _}
import conclave.QuillContext
import conclave.QuillContext._
import conclave.model.{AccountId, Event, GroupId, Rsvp}

import java.util.UUID
import javax.sql.DataSource

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
  val test: ULayer[EventRepository]              = EventRepositoryTest.layer
  val live: URLayer[DataSource, EventRepository] = EventRepositoryLive.layer

  def save(event: Event): ZIO[EventRepository, Throwable, Event] = ZIO.serviceWithZIO[EventRepository](_.save(event))
}

case class EventRepositoryLive(
    newEventHub: Hub[Event],
    rsvpHub: Hub[Rsvp],
    dataSource: DataSource
) extends EventRepository {

  // # EVENTS

  override def allEvents: Task[List[Event]] =
    run(query[Event]).provideService(dataSource)

  override def save(event: Event): Task[Event] =
    run(query[Event].insertValue(lift(event)).returningGenerated(_.id))
      .provideService(dataSource)
      .map(uuid => event.copy(id = uuid))
      .tap(newEventHub.publish)

  // # RSVPS

  override def createRsvp(rsvp: Rsvp): Task[Unit] =
    run(query[Rsvp].insertValue(lift(rsvp))).provideService(dataSource) *>
      rsvpHub.publish(rsvp).unit

  override def rsvps(accountId: AccountId): Task[List[Rsvp]] =
    run(query[Rsvp].filter(_.accountId == lift(accountId))).provideService(dataSource)

  override def removeRsvp(rsvp: Rsvp): Task[Unit] =
    run {
      query[Rsvp].filter(r => r.eventId == lift(rsvp.eventId) && r.accountId == lift(rsvp.accountId)).delete
    }
      .provideService(dataSource)
      .unit

  private def allRsvps: Task[List[Rsvp]] =
    run(query[Rsvp]).provideService(dataSource)

  override def nextEvent(groupId: GroupId): Task[Option[Event]] =
    run(
      query[Event]
        .filter(e => e.groupId == lift(groupId))
        .sortBy(_.time)
    )
      .map(_.headOption)
      .provideService(dataSource)
}

object QueryMuckery extends ZIOAppDefault {
  val gid = GroupId(UUID.fromString("c2aafb0a-7667-457f-a606-e6d73d2e91de"))

  val run =
    EventRepository
      .nextEvent(gid)
      .debug("NEXT EVENT")
      .provide(QuillContext.live, EventRepositoryLive.layer)
}

object EventRepositoryLive {
  val layer: URLayer[DataSource, EventRepository] = {
    for {
      connection  <- ZIO.service[DataSource]
      newEventHub <- Hub.bounded[Event](256)
      rsvpHub     <- Hub.bounded[Rsvp](256)
    } yield EventRepositoryLive(newEventHub, rsvpHub, connection)
  }.toLayer
}
