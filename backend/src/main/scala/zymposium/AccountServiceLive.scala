package zymposium

import zio._
import zymposium.protocol.{AccountInfo, AccountService}

case class AccountServiceLive(appContext: AppContext) extends AccountService {
  override def me: UIO[AccountInfo] =
    for {
      ctx <- appContext.get.someOrFailException.orDie
    } yield AccountInfo(ctx.email)
}

object AccountServiceLive {
  val layer: URLayer[Has[AppContext], Has[AccountService]] =
    (AccountServiceLive.apply _).toLayer
}
