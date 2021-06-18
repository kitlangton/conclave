package zymposium

import zhttp.http._
import zio._
import zio.app.DeriveRoutes
import zio.magic._
import zymposium.protocol._

import CustomPicklers._

object Backend extends App {
  private lazy val eventService      = DeriveRoutes.gen[EventService]
  private lazy val accountService    = DeriveRoutes.gen[AccountService]
  private lazy val userEventsService = DeriveRoutes.gen[UserEventsService]
  private lazy val loginRoutes       = DeriveRoutes.gen[LoginService]
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
        EventServiceLive.layer,
        LoginServiceLive.layer,
        AccountServiceLive.layer,
        UserEventsServiceLive.layer,
        AppContext.live
      )
      .exitCode
}
