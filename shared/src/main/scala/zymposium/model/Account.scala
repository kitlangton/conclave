package zymposium.model

import java.util.UUID

case class Account(
    id: UUID,
    email: String,
    githubAccessToken: Option[String] = None,
    githubRefreshToken: Option[String] = None
)

case class Comment(
    id: UUID,
    text: String,
    accountId: UUID
)

case class NewAccount(email: String)
