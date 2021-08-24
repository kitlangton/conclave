package zymposium.model

import java.time.Instant
import java.util.UUID

final case class Group(
    id: UUID,
    name: String
)

final case class Event(
    id: UUID,
    groupId: UUID,
    title: String,
    description: String,
    time: Instant
    // Zoom Link
    // duration / endTime
)

final case class NewEvent(
    title: String,
    groupId: UUID,
    description: String
)
