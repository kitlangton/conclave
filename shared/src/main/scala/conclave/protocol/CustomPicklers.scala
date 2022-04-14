package conclave.protocol

import boopickle.Default._

import java.time.Instant

object CustomPicklers {
  implicit val datePickler: Pickler[Instant] =
    transformPickler((t: Long) => Instant.ofEpochMilli(t))(_.toEpochMilli)
}
