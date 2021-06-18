package zymposium.protocols

import zio._
import zymposium.AppContext
import zymposium.model.AccountInfo
import zymposium.protocol.AccountProtocol

case class AccountProtocolLive(appContext: AppContext) extends AccountProtocol {
  override def me: UIO[AccountInfo] =
    for {
      ctx <- appContext.get.someOrFailException.orDie
    } yield AccountInfo(ctx.email)
}

object AccountProtocolLive {
  val layer: URLayer[Has[AppContext], Has[AccountProtocol]] =
    (AccountProtocolLive.apply _).toLayer
}
