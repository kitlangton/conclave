package zymposium.protocol

import java.util.UUID

case class Account(
    id: UUID,
    email: String,
    githubAccessToken: Option[String] = None,
    githubRefreshToken: Option[String] = None
)

case class Rsvp(
    accountId: UUID,
    eventId: UUID
)
