package zymposium

import io.getquill.context.ZioJdbc.{DataSourceLayer, QDataSource}
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import zio.{Has, ULayer}

import java.sql.Connection

object QuillContext extends PostgresZioJdbcContext(SnakeCase) {
  val live: ULayer[Has[Connection]] =
    (QDataSource.fromPrefix("postgresDB") >>> DataSourceLayer.live).orDie
}
