package zymposium

import zio.app.DeriveClient
import zymposium.protocol.{AccountService, EventService, LoginService}
import zymposium.protocol.CustomPicklers._

object Clients {
  val eventService: EventService = DeriveClient.gen[EventService]
  val loginService: LoginService = DeriveClient.gen[LoginService]

  val accountService: AccountService = DeriveClient.gen[AccountService]
}
