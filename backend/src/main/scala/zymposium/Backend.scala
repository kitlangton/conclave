package zymposium

import zhttp.http._
import zio._
import zio.app.DeriveRoutes
import zio.magic._
import zymposium.protocol._
import CustomPicklers._
import zymposium.protocols.{AccountProtocolLive, EventProtocolLive, LoginProtocolLive, UserEventsProtocolLive}
import zymposium.repositories.{AccountRepository, EventRepository}

object Backend extends App {
  private lazy val eventService      = DeriveRoutes.gen[EventProtocol]
  private lazy val accountService    = DeriveRoutes.gen[AccountProtocol]
  private lazy val userEventsService = DeriveRoutes.gen[UserEventsProtocol]
  private lazy val loginRoutes       = DeriveRoutes.gen[LoginProtocol]
  private lazy val app = loginRoutes +++ eventService +++
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
