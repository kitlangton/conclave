package zymposium.protocol

import java.time.Instant
import java.util.UUID

case class Event(
    id: UUID,
    title: String,
    description: String,
    time: Instant
)
