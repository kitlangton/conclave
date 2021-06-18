package zymposium.protocol

import zio.UIO
import zymposium.model.AccountInfo

trait AccountProtocol {
  def me: UIO[AccountInfo]
}
