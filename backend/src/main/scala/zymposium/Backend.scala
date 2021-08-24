package zymposium

import zhttp.http._
import zio._
import zio.magic._
import zymposium.protocol._
import CustomPicklers._
import zio.app.DeriveRoutes
import zymposium.protocols.{AccountProtocolLive, EventProtocolLive, LoginProtocolLive, UserEventsProtocolLive}
import zymposium.repositories.{AccountRepository, EventRepository}

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
object Backend extends App {
  private lazy val eventService      = DeriveRoutes.gen[EventProtocol]
  private lazy val accountService    = DeriveRoutes.gen[AccountProtocol]
  private lazy val userEventsService = DeriveRoutes.gen[UserEventsProtocol]
  private lazy val loginRoutes       = DeriveRoutes.gen[LoginProtocol]

  private lazy val app =
    loginRoutes +++ eventService +++
      Authentication.authenticate(HttpApp.forbidden("Not allowed!"), accountService +++ userEventsService)

  val program = for {
    port <- system.envOrElse("PORT", "8088").map(_.toInt).orElseSucceed(8088)
    _    <- zhttp.service.Server.start(port, app)
  } yield ()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program
      .injectCustom(
        QuillContext.live,
        AccountRepository.live,
        EventRepository.live,
        EventProtocolLive.layer,
        LoginProtocolLive.layer,
        AccountProtocolLive.layer,
        UserEventsProtocolLive.layer,
        AppContext.live
      )
      .exitCode
}
