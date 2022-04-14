package conclave

import zhttp.http._
import zhttp._
import zhttp.service._
import zio._
import zio.app.DeriveRoutes
import conclave.protocol._
import conclave.protocols.{AccountProtocolLive, EventProtocolLive, LoginProtocolLive, UserEventsProtocolLive}
import conclave.repositories.{AccountRepository, EventRepository, GroupRepository, PasswordHasher}
import CustomPicklers._

/** - Events
  *   √ Create events
  *   - Add more to the event model
  *     - Add durations
  *     - Add Zoom URLs
  * - RSVPs
  *   √ Create RSVPs
  *   √ Delete RSVPs
  * - Users can view all of their upcoming/past events
  * - Authentication
  * - Logging
  * - Metrics
  * - Monitoring
  * - Make the frontend pretty
  *
  * - Notifications
  *   - Users are notified when a new event gets posted for some
  *     group they follow.
  *   - Users are notified (according to their settings) some period
  *     before an event begins. 1 hr, 1 day, etc.
  *   - Design using traits/stubs
  *   - A background process to fire off notifications at some interval
  *   - Making sure we pick up where we left off
  */
object Backend extends ZIOAppDefault {
  private lazy val eventService      = DeriveRoutes.gen[EventProtocol]
  private lazy val accountService    = DeriveRoutes.gen[AccountProtocol]
  private lazy val userEventsService = DeriveRoutes.gen[UserEventsProtocol]
  private lazy val loginRoutes       = DeriveRoutes.gen[LoginProtocol]

  private lazy val app =
    (loginRoutes ++ eventService ++
      Authentication.authenticate(Http.forbidden("Not allowed!"), accountService ++ userEventsService))
      .contramap[Request] { request =>
        println(request.method)
        println(request.path)
        println(request.headers)
        request
      }
      .catchAll { e =>
        Http.fromZIO(ZIO.debug(s"Server Error: $e")) *>
          Http.empty
      }

  val program = for {
    port <- System.envOrElse("PORT", "8088").map(_.toInt).orElseSucceed(8088)
    _    <- zhttp.service.Server.start(port, app)
  } yield ()

  override val run =
    program
      .provideCustom(
        PasswordHasher.live,
        QuillContext.live,
        AccountRepository.live,
        EventRepository.live,
        EventProtocolLive.layer,
        LoginProtocolLive.layer,
        AccountProtocolLive.layer,
        UserEventsProtocolLive.layer,
        AppContext.live,
        GroupRepository.live
      )
}
