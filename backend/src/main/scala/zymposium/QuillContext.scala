package zymposium

import io.getquill.context.ZioJdbc.{DataSourceLayer, QDataSource}
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import zio.{Has, ULayer}

import java.sql.{Connection, Timestamp, Types}
import java.time.Instant

object QuillContext extends PostgresZioJdbcContext(SnakeCase) {
  val live: ULayer[Has[Connection]] =
    (QDataSource.fromPrefix("postgresDB") >>> DataSourceLayer.live).orDie

  implicit val instantEncoder: Encoder[Instant] =
    encoder(Types.TIMESTAMP, (index, value, row) => row.setTimestamp(index, Timestamp.from(value)))

  implicit val instantDecoder: Decoder[Instant] =
    decoder(row => index => { row.getTimestamp(index).toInstant })

}
