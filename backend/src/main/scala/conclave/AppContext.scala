package conclave

import zio._
import zio.macros.accessible
import conclave.Authentication.Claims

@accessible
trait AppContext {
  def get: UIO[Option[Claims]]
  def set(claims: Claims): UIO[Unit]
}

object AppContext {
  val live: ULayer[AppContext] = {
    for {
      fiberRef <- FiberRef.make(Option.empty[Claims])
    } yield AppContextLive(fiberRef)
  }.toLayer

  private final case class AppContextLive(fiberRef: FiberRef[Option[Claims]]) extends AppContext {
    override def get: UIO[Option[Claims]] = fiberRef.get

    override def set(claims: Claims): UIO[Unit] =
      fiberRef.set(Some(claims))
  }
}
