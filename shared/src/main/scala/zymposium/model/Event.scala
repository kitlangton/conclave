package zymposium.model

import java.time.Instant
import java.util.UUID

case class Event(
    id: UUID,
    title: String,
    description: String,
    time: Instant
)

case class NewEvent(title: String, description: String)
