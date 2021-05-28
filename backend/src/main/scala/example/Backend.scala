package example

import example.protocol.{Event, EventService, NewEvent}
import zhttp.http._
import zio._
import zio.app.DeriveRoutes
import zio.console._
import zio.magic._
import zio.random.Random
import example.protocol.CustomPicklers._

import java.time.Instant
import java.util.UUID

/** Meetup Clone
  * - Account
  *   - UUID
  *   - Email
  * - Event
  *   - UUID
  *   - Title
  *   - Description
  *   - Time
  *   - Zoom
  *   - Creator account_id
  * - RSVP
  *   - account_id
  *   - event_id
  *
  * QUESTIONS:
  *  - How to use a connection pool with Quill?
  *  - How to batch and pipeline queries with ZQuery.
  */
object Backend extends App {
  private val httpApp: HttpApp[Has[EventService], Throwable] =
    DeriveRoutes.gen[EventService].cmap[Request] { request =>
      println(s"RECEIVED REQUEST $request")
      request
    }

  val eventFixtures = List(
    Event(UUID.randomUUID(), "Zymposium Berlin", "Zymposium in Berlin", Instant.now()),
    Event(
      UUID.randomUUID(),
      "Zymposium New Zealand",
      "Zymposium in New Zealand",
      Instant.now().plusSeconds(999999)
    ),
    Event(UUID.randomUUID(), "Zymposium Mars", "Zymposium on Mars", Instant.now().plusSeconds(999999999))
  )

  val program = for {
    port <- system.envOrElse("PORT", "8088").map(_.toInt).orElseSucceed(8088)
    _    <- ZIO.foreachPar_(eventFixtures)(EventRepository.save)
    _    <- zhttp.service.Server.start(port, httpApp)
  } yield ()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.injectCustom(QuillContext.live, EventRepository.live, EventServiceLive.layer).exitCode
}

case class EventServiceLive(eventRepository: EventRepository, random: zio.random.Random.Service) extends EventService {
  override def magicNumber: UIO[Int] = random.nextInt

  override def allEvents: Task[List[Event]] =
    eventRepository.allEvents

  override def createEvent(newEvent: NewEvent): Task[Event] =
    eventRepository.save(Event(UUID.randomUUID(), newEvent.title, newEvent.description, Instant.now()))
}

object EventServiceLive {
  val layer: URLayer[Has[EventRepository] with Random, Has[EventService]] =
    (EventServiceLive.apply _).toLayer[EventService]
}
