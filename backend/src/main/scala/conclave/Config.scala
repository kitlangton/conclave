package conclave

import zio._
import zio.config._
import zio.config.yaml._
import zio.config.magnolia._

import java.io.File

case class Config(usernames: List[String])

object Config {
  val descriptor: ConfigDescriptor[Config] =
    DeriveConfigDescriptor.descriptor[Config]

  val live: ZLayer[System, Nothing, Config] =
    YamlConfig.fromFile(new File("../application.conf.yaml"), descriptor).orDie

  val service: URIO[Config, Config] = ZIO.service[Config]
}
