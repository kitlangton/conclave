package conclave.protocols

import zio._
import conclave.Authentication.{Claims, jwtEncode}
import conclave.model.{Email, JwtToken, Password}
import conclave.protocol.LoginProtocol
import conclave.repositories.{AccountRepository, PasswordHasher}

case class LoginProtocolLive(passwordHasher: PasswordHasher, accountRepository: AccountRepository)
    extends LoginProtocol {

  override def login(email: Email, password: Password): IO[String, JwtToken] =
    for {
      account <- accountRepository
                   .findByEmail(email = email)
                   .orDie
                   .someOrFail(s"INVALID PASSWORD OR USERNAME")
      _ <- ZIO
             .fail("INVALID PASSWORD OR USERNAME")
             .when(account.passwordHash != passwordHasher.hashPassword(password))
    } yield JwtToken(jwtEncode(Claims(email, account.id)))

  override def register(email: Email, password: Password): IO[String, JwtToken] =
    accountRepository
      .create(email, password)
      .orDie
      .map(account => JwtToken(jwtEncode(Claims(email, account.id))))

}

object LoginProtocolLive {
  val layer: URLayer[PasswordHasher with AccountRepository, LoginProtocol] =
    (LoginProtocolLive.apply _).toLayer[LoginProtocol]
}
