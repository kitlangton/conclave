package conclave.protocols

import conclave.AppContext
import conclave.model.{AccountInfo, Group}
import conclave.protocol.AccountProtocol
import conclave.repositories.GroupRepository
import zio._

import javax.sql.DataSource

case class AccountProtocolLive(
    appContext: AppContext,
    dataSource: DataSource,
    groupRepository: GroupRepository,
    clock: Clock
) extends AccountProtocol {
  import conclave.QuillContext._

  override def me: Task[AccountInfo] =
    appContext.get.someOrFailException
      .map(ctx => AccountInfo(ctx.email))

  override def groups: Task[List[Group]] =
    run(query[Group]).debug("OH NO").provideService(dataSource)

  override def createGroup(name: String, description: String): Task[Group] =
    for {
      ctx   <- appContext.get.someOrFailException
      group <- groupRepository.create(ctx.accountId, name, description)
    } yield group

  override def group(slug: String): Task[Group] =
    groupRepository.get(slug).someOrFailException
}

object AccountProtocolLive {
  val layer =
    (AccountProtocolLive.apply _).toLayer
}
