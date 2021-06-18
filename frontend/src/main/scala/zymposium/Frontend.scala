package zymposium

import com.raquo.laminar.api.L._
import zio._
import zio.app.DeriveClient
import zymposium.Clients.eventService
import LaminarZioSyntax._
import zymposium.protocol.{Account, AccountInfo, AccountService}

object Frontend {
  val runtime    = Runtime.default
  val accountVar = Var(Option.empty[(AccountInfo, String)])

  sealed trait Mode

  object Mode {
    case object User  extends Mode
    case object Admin extends Mode
  }

  private val modeVar = Var[Mode](Mode.Admin)

  def view: Div =
    div(
      div(
        margin("0 auto"),
        marginBottom("24px"),
        width("200px"),
        display.flex,
        button("USER", onClick --> { _ => modeVar.set(Mode.User) }),
        button("ADMIN", onClick --> { _ => modeVar.set(Mode.Admin) })
      ),
      child <-- modeVar.signal.map {
        case Mode.User  => userView
        case Mode.Admin => AdminPage()
      }
    )

  private def userView = {
    div(
      LoginForm(accountVar),
      child.maybe <-- accountVar.signal.map {
        _.map { case (_, token) =>
          EventListing(token)
        }
      }
    )
  }
}
