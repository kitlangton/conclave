package conclave.protocol

import zio.IO
import zio.macros.accessible
import conclave.model.{Email, JwtToken, Password}

@accessible
trait LoginProtocol {
  def login(email: Email, password: Password): IO[String, JwtToken]
  def register(email: Email, password: Password): IO[String, JwtToken]
}
