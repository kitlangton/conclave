package zymposium.model

import zio.json.{DeriveJsonCodec, JsonCodec}

import java.util.UUID

final case class CommentId(uuid: UUID) extends AnyVal
final case class AccountId(uuid: UUID) extends AnyVal
final case class EventId(uuid: UUID)   extends AnyVal
final case class GroupId(uuid: UUID)   extends AnyVal

object AccountId {
  def random: AccountId = AccountId(UUID.randomUUID())

  implicit val codec: JsonCodec[AccountId] = DeriveJsonCodec.gen
}

final case class Email(string: String) extends AnyVal

object Email {
  implicit val codec: JsonCodec[Email] = DeriveJsonCodec.gen
}

final case class Password(string: String)     extends AnyVal
final case class PasswordHash(string: String) extends AnyVal

case class Account(
    id: AccountId,
    email: Email,
    passwordHash: PasswordHash,
    githubAccessToken: Option[String] = None,
    githubRefreshToken: Option[String] = None
)

case class Comment(
    id: CommentId,
    text: String,
    accountId: AccountId
)

case class NewAccount(email: Email)
