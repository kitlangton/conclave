package conclave.protocols

import zio._
import conclave.AppContext
import conclave.Authentication.Claims
import conclave.model.{EventId, Rsvp}
import conclave.protocol.UserEventsProtocol
import conclave.repositories.EventRepository

import java.util.UUID

case class UserEventsProtocolLive(appContext: AppContext, eventRepo: EventRepository) extends UserEventsProtocol {
  override def rsvpedEvents: UIO[List[Rsvp]] =
    for {
      ctx   <- getAccountContext
      rsvps <- eventRepo.rsvps(ctx.accountId).orDie
    } yield rsvps

  override def rsvp(event: EventId): UIO[Rsvp] =
    for {
      ctx <- getAccountContext
      rsvp = Rsvp(accountId = ctx.accountId, eventId = event)
      _   <- eventRepo.createRsvp(rsvp).orDie
    } yield rsvp

  override def removeRsvp(event: EventId): UIO[Unit] =
    for {
      ctx <- getAccountContext
      _   <- eventRepo.removeRsvp(Rsvp(accountId = ctx.accountId, eventId = event)).orDie
    } yield ()

  private def getAccountContext: UIO[Claims] =
    appContext.get.someOrFailException.orDie
}

object UserEventsProtocolLive {
  val layer: URLayer[AppContext with EventRepository, UserEventsProtocol] =
    (UserEventsProtocolLive.apply _).toLayer
}
