package zymposium.protocol

import zio.IO
import zio.macros.accessible
import zymposium.model.{Email, JwtToken, Password}

@accessible
trait LoginProtocol {
  def login(email: Email, password: Password): IO[String, JwtToken]
  def register(email: Email, password: Password): IO[String, JwtToken]
}
