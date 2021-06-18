package zymposium

import java.util.UUID

case class UserContext(token: String, email: String, accountId: UUID)
