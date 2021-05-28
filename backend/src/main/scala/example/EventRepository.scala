package example

import example.protocol.Event
import io.getquill.context.ZioJdbc.QDataSource
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import zio._
import zio.blocking.Blocking
import zio.magic._

import java.sql.Connection

trait EventRepository {
  def allEvents: Task[List[Event]]

  def save(event: Event): Task[Event]
}

object EventRepository {
  val test: ULayer[Has[EventRepository]]                                              = EventRepositoryTest.layer
  val live: URLayer[Has[Connection] with Has[Blocking.Service], Has[EventRepository]] = EventRepositoryLive.layer

  def save(event: Event): ZIO[Has[EventRepository], Throwable, Event] = ZIO.serviceWith[EventRepository](_.save(event))
}

object QuillContext extends PostgresZioJdbcContext(SnakeCase) {
  val live: ZLayer[zio.blocking.Blocking, Nothing, Has[Connection]] =
    (QDataSource.fromPrefix("postgresDB") >>> QDataSource.toConnection).orDie
}

import QuillContext._

case class EventRepositoryLive(connection: Connection, blocking: Blocking.Service) extends EventRepository {
  lazy val env: Has[Connection] with Has[Blocking.Service] =
    Has(connection) ++ Has(blocking)

  val allEventsQuery = quote { query[Event] }

  override def allEvents: Task[List[Event]] =
    QuillContext
      .run(allEventsQuery)
      .provide(env)

  override def save(event: Event): Task[Event] =
    QuillContext
      .run {
        quote {
          query[Event].insert(lift(event)).returningGenerated(_.id)
        }
      }
      .provide(env)
      .map { uuid =>
        event.copy(id = uuid)
      }
}

object EventRepositoryLive {
  val layer: URLayer[Has[Connection] with Has[Blocking.Service], Has[EventRepository]] =
    (EventRepositoryLive.apply _).toLayer
}
