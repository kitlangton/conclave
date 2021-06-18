package zymposium

import zio.blocking.Blocking
import zio.stream.{UStream, ZStream}
import zio.{query => _, _}
import zymposium.protocol.Account

import java.sql.{Connection, Timestamp, Types}
import java.time.Instant

trait AccountRepository {
  def get(email: String): Task[Option[Account]]

  def allAccountsStream: UStream[Account]

  def allAccounts: Task[List[Account]]

  def save(account: Account): Task[Account]
}

object AccountRepository {
  val live: URLayer[Has[Connection] with Has[Blocking.Service], Has[AccountRepository]] = AccountRepositoryLive.layer

  def save(account: Account): ZIO[Has[AccountRepository], Throwable, Account] =
    ZIO.serviceWith[AccountRepository](_.save(account))
}

import QuillContext._

case class AccountRepositoryLive(accountHub: Hub[Account], connection: Connection, blocking: Blocking.Service)
    extends AccountRepository {
  implicit val instantEncoder: Encoder[Instant] =
    encoder(Types.TIMESTAMP, (index, value, row) => row.setTimestamp(index, Timestamp.from(value)))
  implicit val instantDecoder: Decoder[Instant] = decoder((index, row) => { row.getTimestamp(index).toInstant })

  lazy val env: Has[Connection] with Has[Blocking.Service] =
    Has(connection) ++ Has(blocking)

  private val allAccountsQuery = quote { query[Account] }

  override def allAccounts: Task[List[Account]] =
    run(allAccountsQuery).provide(env)

  override def save(account: Account): Task[Account] =
    for {
      account <- saveAccountToDatabase(account)
      _       <- accountHub.publish(account)
    } yield account

  private def saveAccountToDatabase(account: Account) = {
    run { query[Account].insert(lift(account)).returningGenerated(_.id) }
      .provide(env)
      .map { uuid => account.copy(id = uuid) }
  }

  override def allAccountsStream: UStream[Account] =
    ZStream.fromEffect(allAccounts.orDie.map(Chunk.fromIterable)).flattenChunks ++
      ZStream.fromHub(accountHub)

  override def get(email: String): Task[Option[Account]] =
    run { query[Account].filter(_.email == lift(email)) }
      .provide(env)
      .map(_.headOption)
}

object AccountRepositoryLive {
  val layer: URLayer[Has[Connection] with Has[Blocking.Service], Has[AccountRepository]] = {
    for {
      conn       <- ZIO.service[Connection]
      blocking   <- ZIO.service[Blocking.Service]
      accountHub <- Hub.bounded[Account](128)
    } yield AccountRepositoryLive(accountHub, conn, blocking)
  }.toLayer
}
