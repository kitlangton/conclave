package conclave.model

import java.time.Instant
import java.util.UUID

final case class Group(
    id: GroupId,
    ownerId: AccountId,
    name: String,
    description: String,
    slug: String
)

final case class Event(
    id: EventId,
    groupId: GroupId,
    title: String,
    description: String,
    time: Instant
    // Zoom Link
    // duration / endTime
)

final case class NewEvent(
    title: String,
    groupId: GroupId,
    description: String
)
