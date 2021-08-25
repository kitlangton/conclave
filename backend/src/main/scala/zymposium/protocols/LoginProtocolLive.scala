package zymposium.protocols

import zio._
import zymposium.Authentication.{Claims, jwtEncode}
import zymposium.model.{Email, JwtToken, Password}
import zymposium.protocol.LoginProtocol
import zymposium.repositories.{AccountRepository, PasswordHasher}

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
  val layer: URLayer[Has[PasswordHasher] with Has[AccountRepository], Has[LoginProtocol]] =
    (LoginProtocolLive.apply _).toLayer[LoginProtocol]
}
