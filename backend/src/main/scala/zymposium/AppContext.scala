package zymposium

import zio._
import zio.macros.accessible
import zymposium.Authentication.Claims

@accessible
trait AppContext {
  def get: UIO[Option[Claims]]
  def set(claims: Claims): UIO[Unit]
}

object AppContext {
  val live: ULayer[Has[AppContext]] = {
    for {
      fiberRef <- FiberRef.make(Option.empty[Claims])
    } yield new AppContext {
      override def get: UIO[Option[Claims]] = fiberRef.get

      override def set(claims: Claims): UIO[Unit] = fiberRef.set(Some(claims))
    }
  }.toLayer
}
