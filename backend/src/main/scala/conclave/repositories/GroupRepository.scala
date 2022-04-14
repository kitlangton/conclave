package conclave.repositories

import conclave.QuillContext._
import conclave.model.{AccountId, Group, GroupId}
import zio.macros.accessible
import zio.{query => _, _}

import javax.sql.DataSource

@accessible
trait GroupRepository {
  def create(owner: AccountId, name: String, description: String): Task[Group]
  def get(slug: String): Task[Option[Group]]
}

object GroupRepository {

  val live = GroupRepositoryLive.layer
}

case class GroupRepositoryLive(
    random: Random,
    dataSource: DataSource
) extends GroupRepository {

  def slugify(name: String): String =
    name.toLowerCase.replaceAll("[^a-z0-9]+", "-")

  def create(owner: AccountId, name: String, description: String): Task[Group] =
    for {
      groupId <- random.nextUUID
      group    = Group(GroupId(groupId), owner, name, description, slugify(name))
      _       <- ZIO.debug("ABOUT TO CREATE: " + group)
      _       <- run(query[Group].insertValue(lift(group))).provideService(dataSource)
      _       <- ZIO.debug("CREATED GROUP: " + group)
    } yield group

  override def get(slug: String): Task[Option[Group]] =
    run(query[Group].filter(g => g.slug == lift(slug)))
      .provideService(dataSource)
      .map(_.headOption)
}

object GroupRepositoryLive {
  val layer: ZLayer[DataSource with Random, Nothing, GroupRepository] = ZLayer {
    for {
      random     <- ZIO.service[Random]
      dataSource <- ZIO.service[DataSource]
    } yield GroupRepositoryLive(random, dataSource): GroupRepository
  }
}
