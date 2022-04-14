package conclave.repositories

import zio.ZLayer
import conclave.model.{Password, PasswordHash}
import com.github.t3hnar.bcrypt._

final case class PasswordHasher(salt: String) {
  def hashPassword(password: Password): PasswordHash =
    PasswordHash(password.string.bcryptBounded(salt))
}

object PasswordHasher {
  val live = ZLayer.succeed(PasswordHasher("$2a$10$OCzRv.VFEKORkk2SG8r1au"))
}
