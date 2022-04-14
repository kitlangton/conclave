package conclave

import com.raquo.laminar.api.L._
import conclave.model.AccountInfo
import conclave.protocol.AccountProtocol
import org.scalajs.dom.window.localStorage
import zio.app.{ClientConfig, DeriveClient}
import zio.interop.laminar.ZioOps
import conclave.protocol.CustomPicklers._

object AccountPersistence {
  private val userTokenVar = Var(Option.empty[String])
  val $userToken           = userTokenVar.signal
  val $accountInfo: Signal[Option[AccountInfo]] = $userToken.flatMap {
    case Some(token) =>
      println(s"GOT TOKEN ${token}")
      DeriveClient
        .gen[AccountProtocol](ClientConfig(Some(token)))
        .me
        .debug("ME")
        .catchAllDefect { e =>
          println(s"ERROR!!!! ${e.getMessage}")
          throw new Error("OOPS")
        }
        .toEventStream
        .map(Some(_))
    case None =>
      EventStream.fromValue(Option.empty[AccountInfo])
  }
    .toSignal(None)
    .recover { _ =>
      userTokenVar.set(None)
      None
    }

  val $signedIn = $accountInfo.map(_.isDefined)

  def setToken(string: String): Unit = {
    userTokenVar.set(Some(string))
    localStorage.setItem("userToken", string)
  }

  def logout(): Unit = {
    userTokenVar.set(None)
    localStorage.removeItem("userToken")
    Router.router.pushState(Page.HomePage)
  }

  def load(): Unit = {
    val token = Option(localStorage.getItem("userToken"))
    userTokenVar.set(token)
  }

  load()
}
