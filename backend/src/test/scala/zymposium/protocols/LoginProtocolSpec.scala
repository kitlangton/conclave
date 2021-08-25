package zymposium.protocols

import zio.magic._
import zio.test._
import zymposium.model.{Email, Password}
import zymposium.protocol.LoginProtocol
import zymposium.repositories.{AccountRepository, PasswordHasher}
import zymposium.{Authentication, QuillContext}

object LoginProtocolSpec extends DefaultRunnableSpec {
  override def spec =
    suite("LoginProtocol")(
      testM("creates account and logs in") {
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
      testM("fails when invalid password") {
        val email = Email("jimmy@clown.gov")
        for {
          _       <- LoginProtocol.register(email, Password("rightPassword"))
          failure <- LoginProtocol.login(email, Password("wrongPassword")).flip
        } yield assertTrue(
          failure == "INVALID PASSWORD OR USERNAME"
        )
      },
      testM("fails when nonexistent account") {
        for {
          failure <- LoginProtocol.login(Email("nobodoy@clown.gov"), Password("password")).flip
        } yield assertTrue(
          failure == "INVALID PASSWORD OR USERNAME"
        )
      }
    ).inject(
      LoginProtocolLive.layer,
      PasswordHasher.live,
      AccountRepository.live,
      QuillContext.live
    )
}
