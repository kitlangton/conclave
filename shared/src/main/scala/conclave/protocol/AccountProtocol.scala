package conclave.protocol

import zio._
import conclave.model.{AccountInfo, Group}

trait AccountProtocol {

  def me: Task[AccountInfo]

  def groups: Task[List[Group]]

  def createGroup(name: String, description: String): Task[Group]

  def group(slug: String): Task[Group]

}
