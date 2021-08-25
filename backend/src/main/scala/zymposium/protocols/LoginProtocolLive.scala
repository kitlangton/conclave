package zymposium.protocols

import zio._
import zymposium.Authentication.{Claims, jwtEncode}
import zymposium.model.{Email, JwtToken, Password}
import zymposium.protocol.LoginProtocol
import zymposium.repositories.AccountRepository

case class LoginProtocolLive(accountRepository: AccountRepository) extends LoginProtocol {
  override def login(email: Email, password: Password): IO[String, JwtToken] =
    if (password.string == "123") {
      for {
        account <- accountRepository.get(email = email).someOrFailException orElseFail s"No User with Email ${email}"
        r       <- UIO(JwtToken(jwtEncode(Claims(email, account.id))))
      } yield r
    } else
      ZIO.fail("INVALID PASSWORD OR USERNAME")
}

object LoginProtocolLive {
  val layer: URLayer[Has[AccountRepository], Has[LoginProtocol]] =
    (LoginProtocolLive.apply _).toLayer[LoginProtocol]
}
