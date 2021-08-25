package zymposium.model

import java.time.Instant

final case class Group(
    id: GroupId,
    name: String
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
