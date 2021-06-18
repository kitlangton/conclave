package zymposium

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zhttp.http.{Http, HttpApp, Request}
import zio.Has
import zio.json._

import java.time.Clock
import java.util.UUID

object Authentication {
  case class Claims(email: String, accountId: UUID)

  object Claims {
    implicit val codec: JsonCodec[Claims] = DeriveJsonCodec.gen[Claims]
  }

  val SECRET_KEY = "wobbly-chortling-prince"

  implicit val clock: Clock = Clock.systemUTC

  // Helper to encode the JWT token
  def jwtEncode(claims: Claims): String = {
    val json  = claims.toJson
    val claim = JwtClaim { json }.issuedNow.expiresIn(99999999)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)
  }

  // Helper to decode the JWT token
  def jwtDecode(token: String): Option[JwtClaim] = {
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption
  }

  def authenticate[R, E](
      fail: HttpApp[R, E],
      success: HttpApp[R, E]
  ): HttpApp[R with Has[AppContext], E] =
    Http.fromFunction { (r: Request) =>
      r.getHeader("authorization")
        .flatMap {
          _.value match {
            case s"Bearer $token" =>
              println(s"BEARER ${token}")
              jwtDecode(token).flatMap { jwt =>
                println(s"TOKEN ${jwt}")
                jwt.content.fromJson[Claims].toOption
              }
            case _ => None
          }
        }
        .fold {
          Http.fromEffect(AppContext.get) *> fail
        } { claims =>
          Http.fromEffect(AppContext.set(claims)) *> success
        }
    }.flatten

}
