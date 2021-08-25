package zymposium.protocol

import zio.IO
import zymposium.model.{Email, JwtToken, Password}

trait LoginProtocol {
  def login(email: Email, password: Password): IO[String, JwtToken]
}
