package conclave.repositories

import zio.macros.accessible
import zio.{query => _, _}
import conclave.QuillContext._
import conclave.model.{Account, AccountId, Email, Password}

import javax.sql.DataSource

@accessible
trait AccountRepository {
  def create(email: Email, password: Password): Task[Account]
  def findByEmail(email: Email): Task[Option[Account]]
  def save(account: Account): Task[Account]
}

object AccountRepository {
  val live: URLayer[PasswordHasher with DataSource, AccountRepository] =
    AccountRepositoryLive.layer

  def save(account: Account): ZIO[AccountRepository, Throwable, Account] =
    ZIO.serviceWithZIO[AccountRepository](_.save(account))
}

case class AccountRepositoryLive(
    accountHub: Hub[Account],
    dataSource: DataSource,
    passwordHasher: PasswordHasher
) extends AccountRepository {

  override def save(account: Account): Task[Account] =
    for {
      account <- saveAccountToDatabase(account)
      _       <- accountHub.publish(account)
    } yield account

  private def saveAccountToDatabase(account: Account) =
    run(query[Account].insertValue(lift(account)).returningGenerated(_.id))
      .provideService(dataSource)
      .map(uuid => account.copy(id = uuid))

  override def findByEmail(email: Email): Task[Option[Account]] =
    run(query[Account].filter(_.email == lift(email)))
      .provideService(dataSource)
      .map(_.headOption)

  override def create(email: Email, password: Password): Task[Account] = {
    val passwordHash = passwordHasher.hashPassword(password)
    val account      = Account(AccountId.random, email, passwordHash)
    run(query[Account].insertValue(lift(account))).provideService(dataSource).as(account)
  }
}

object AccountRepositoryLive {
  val layer: URLayer[PasswordHasher with DataSource, AccountRepository] = {
    for {
      conn       <- ZIO.service[DataSource]
      hasher     <- ZIO.service[PasswordHasher]
      accountHub <- Hub.bounded[Account](128)
    } yield AccountRepositoryLive(accountHub, conn, hasher)
  }.toLayer[AccountRepository]
}
