package conclave

import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.{Escape, NamingStrategy, PostgresZioJdbcContext, SnakeCase}
import zio.ZLayer

import javax.sql.DataSource

object QuillContext extends PostgresZioJdbcContext(NamingStrategy(SnakeCase, Escape)) {
  val live: ZLayer[Any, Nothing, DataSource] =
    DataSourceLayer.fromPrefix("postgresDB").orDie
}
