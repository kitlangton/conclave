package zymposium

import com.raquo.laminar.api.L._
import zio.app.DeriveClient
import zio.{Runtime, UIO}
import zymposium.Clients.eventService
import LaminarZioSyntax._
import zymposium.protocol.{Account, AccountInfo, AccountService}

case class LoginForm(accountVar: Var[Option[(AccountInfo, String)]]) extends Component {
  private val allAccountsVar = Var(List.empty[Account])

  override def body: HtmlElement =
    div(
      subscribe,
      div(
        padding("12px"),
        background("#222"),
        borderRadius("4px"),
        children <-- allAccountsVar.signal.split(_.id) { (_, account, _) =>
          div(
            cursor.pointer,
            div(
              cls("account"),
              cls.toggle("account-logged-in") <-- accountVar.signal.map(_.exists(_._1.email == account.email)),
              account.email
            ),
            onClick --> { _ =>
              (for {
                token       <- Clients.loginService.login(account.email, "123").debug("LOGIN")
                accountInfo <- DeriveClient.gen[AccountService](Some(token.jwtString)).me
                _           <- UIO(accountVar.set(Some(accountInfo -> token.jwtString)))
              } yield ()).runAsync()
            }
          )
        }
      )
    )

  private val subscribe =
    onMountCallback { (_: MountContext[HtmlElement]) =>
      Runtime.default.unsafeRunAsync_ {
        eventService.allAccountsStream.tap { account => UIO(allAccountsVar.update(account :: _)) }.runDrain
      }
    }
}
