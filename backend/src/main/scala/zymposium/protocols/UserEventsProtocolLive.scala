package zymposium.protocols

import zio._
import zymposium.AppContext
import zymposium.Authentication.Claims
import zymposium.model.Rsvp
import zymposium.protocol.UserEventsProtocol
import zymposium.repositories.EventRepository

import java.util.UUID

case class UserEventsProtocolLive(appContext: AppContext, eventRepo: EventRepository) extends UserEventsProtocol {
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

object UserEventsProtocolLive {
  val layer: URLayer[Has[AppContext] with Has[EventRepository], Has[UserEventsProtocol]] =
    (UserEventsProtocolLive.apply _).toLayer
}
