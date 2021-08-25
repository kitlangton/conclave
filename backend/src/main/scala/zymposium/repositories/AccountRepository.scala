package zymposium.repositories

import zio.macros.accessible
import zio.{query => _, _}
import zymposium.QuillContext._
import zymposium.model.{Account, AccountId, Email, Password}

import java.sql.Connection

@accessible
trait AccountRepository {
  def create(email: Email, password: Password): Task[Account]
  def findByEmail(email: Email): Task[Option[Account]]
  def save(account: Account): Task[Account]
}

object AccountRepository {
  val live: URLayer[Has[PasswordHasher] with Has[Connection], Has[AccountRepository]] =
    AccountRepositoryLive.layer

  def save(account: Account): ZIO[Has[AccountRepository], Throwable, Account] =
    ZIO.serviceWith[AccountRepository](_.save(account))
}

case class AccountRepositoryLive(
    accountHub: Hub[Account],
    connection: Connection,
    passwordHasher: PasswordHasher
) extends AccountRepository {

  lazy val env: Has[Connection] = Has(connection)

  override def save(account: Account): Task[Account] =
    for {
      account <- saveAccountToDatabase(account)
      _       <- accountHub.publish(account)
    } yield account

  private def saveAccountToDatabase(account: Account) =
    run(query[Account].insert(lift(account)).returningGenerated(_.id))
      .provide(env)
      .map(uuid => account.copy(id = uuid))

  override def findByEmail(email: Email): Task[Option[Account]] =
    run(query[Account].filter(_.email == lift(email)))
      .provide(env)
      .map(_.headOption)

  override def create(email: Email, password: Password): Task[Account] = {
    val passwordHash = passwordHasher.hashPassword(password)
    val account      = Account(AccountId.random, email, passwordHash)
    run(query[Account].insert(lift(account)))
      .provide(env)
      .as(account)
  }
}

object AccountRepositoryLive {
  val layer: URLayer[Has[PasswordHasher] with Has[Connection], Has[AccountRepository]] = {
    for {
      conn       <- ZIO.service[Connection]
      hasher     <- ZIO.service[PasswordHasher]
      accountHub <- Hub.bounded[Account](128)
    } yield AccountRepositoryLive(accountHub, conn, hasher)
  }.toLayer[AccountRepository]
}
