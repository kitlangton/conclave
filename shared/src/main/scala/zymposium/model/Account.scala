package zymposium.model

import zio.json.{DeriveJsonCodec, JsonCodec}

import java.util.UUID

final case class CommentId(uuid: UUID) extends AnyVal
final case class AccountId(uuid: UUID) extends AnyVal
final case class EventId(uuid: UUID)   extends AnyVal
final case class GroupId(uuid: UUID)   extends AnyVal

object AccountId {
  implicit val codec: JsonCodec[AccountId] = DeriveJsonCodec.gen
}

final case class Email(string: String) extends AnyVal

object Email {
  implicit val codec: JsonCodec[Email] = DeriveJsonCodec.gen
}

final case class Password(string: String) extends AnyVal

case class Account(
    id: AccountId,
    email: Email,
    githubAccessToken: Option[String] = None,
    githubRefreshToken: Option[String] = None
)

case class Comment(
    id: CommentId,
    text: String,
    accountId: AccountId
)

case class NewAccount(email: Email)
