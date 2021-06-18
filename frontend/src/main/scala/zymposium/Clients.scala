package zymposium

import zio.app.DeriveClient
import zymposium.protocol.{AccountProtocol, EventProtocol, LoginProtocol}
import zymposium.protocol.CustomPicklers._

object Clients {
  val eventService: EventProtocol = DeriveClient.gen[EventProtocol]
  val loginService: LoginProtocol = DeriveClient.gen[LoginProtocol]

  val accountService: AccountProtocol = DeriveClient.gen[AccountProtocol]
}
