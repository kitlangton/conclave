package zymposium

import zio._
import zymposium.Authentication.{Claims, jwtEncode}
import zymposium.protocol.{JwtToken, LoginService}

case class LoginServiceLive(accountRepository: AccountRepository) extends LoginService {
  override def login(email: String, password: String): IO[String, JwtToken] =
    if (password == "123") {
      for {
        account <- accountRepository.get(email = email).someOrFailException orElseFail s"No User with Email ${email}"
        r       <- UIO(JwtToken(jwtEncode(Claims(email, account.id))))
      } yield r
    } else
      ZIO.fail("INVALID PASSWORD OR USERNAME")
}

object LoginServiceLive {
  val layer: URLayer[Has[AccountRepository], Has[LoginService]] =
    (LoginServiceLive.apply _).toLayer[LoginService]
}
