package conclave.protocol

import boopickle.Default._

import java.time.Instant

object CustomPicklers {
  implicit val datePickler: Pickler[Instant] =
    transformPickler((t: Long) => Instant.ofEpochMilli(t))(_.toEpochMilli)

  implicit val UUIDPickler: Pickler[java.util.UUID] =
    transformPickler((t: String) => java.util.UUID.fromString(t))(_.toString)
}
