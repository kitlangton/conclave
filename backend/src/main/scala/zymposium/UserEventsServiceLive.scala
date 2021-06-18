package zymposium

import zio._
import zymposium.Authentication.Claims
import zymposium.protocol.{Rsvp, UserEventsService}

import java.util.UUID

case class UserEventsServiceLive(appContext: AppContext, eventRepo: EventRepository) extends UserEventsService {
  override def rsvpedEvents: UIO[List[Rsvp]] =
    for {
      ctx   <- getAccountContext
      rsvps <- eventRepo.rsvps(ctx.accountId).orDie
    } yield rsvps

  override def rsvp(event: UUID): UIO[Rsvp] =
    for {
      ctx <- getAccountContext
      rsvp = Rsvp(accountId = ctx.accountId, eventId = event)
      _ <- eventRepo.createRsvp(rsvp).orDie
    } yield rsvp

  override def removeRsvp(event: UUID): UIO[Unit] =
    for {
      ctx <- getAccountContext
      _   <- eventRepo.removeRsvp(Rsvp(accountId = ctx.accountId, eventId = event)).orDie
    } yield ()

  private def getAccountContext: UIO[Claims] = {
    appContext.get.someOrFailException.orDie
  }
}

object UserEventsServiceLive {
  val layer: URLayer[Has[AppContext] with Has[EventRepository], Has[UserEventsService]] =
    (UserEventsServiceLive.apply _).toLayer
}
