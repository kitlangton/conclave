package zymposium.protocol

import zio.IO
import zymposium.model.JwtToken

trait LoginProtocol {
  def login(username: String, password: String): IO[String, JwtToken]
}
