package conclave.protocols

import zio.test._
import conclave.model.{Email, Password}
import conclave.protocol.LoginProtocol
import conclave.repositories.{AccountRepository, PasswordHasher}
import conclave.{Authentication, QuillContext}

object LoginProtocolSpec extends DefaultRunnableSpec {
  override def spec =
    suite("LoginProtocol")(
      test("creates account and logs in") {
        val email    = Email("bobo@clown.gov")
        val password = Password("hunter22")
        for {
          jwt1 <- LoginProtocol.register(email, password)
          jwt2 <- LoginProtocol.login(email, password)
        } yield assertTrue(
          jwt1 == jwt2,
          Authentication.decodeToken(jwt2.jwtString).get.email == email
        )
      },
      test("fails when invalid password") {
        val email = Email("jimmy@clown.gov")
        for {
          _       <- LoginProtocol.register(email, Password("rightPassword"))
          failure <- LoginProtocol.login(email, Password("wrongPassword")).flip
        } yield assertTrue(
          failure == "INVALID PASSWORD OR USERNAME"
        )
      },
      test("fails when nonexistent account") {
        for {
          failure <- LoginProtocol.login(Email("nobodoy@clown.gov"), Password("password")).flip
        } yield assertTrue(
          failure == "INVALID PASSWORD OR USERNAME"
        )
      }
    ).provide(
      LoginProtocolLive.layer,
      PasswordHasher.live,
      AccountRepository.live,
      QuillContext.live
    )
}
