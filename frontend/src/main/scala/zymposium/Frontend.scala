package zymposium

import com.raquo.laminar.api.L._
import components.Component
import zio._
import zymposium.events.EventListing
import zymposium.model.AccountInfo
import zymposium.pages.{AdminPage, UserPage}

object Frontend {
  val runtime = Runtime.default

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
        case Mode.User  => UserPage()
        case Mode.Admin => AdminPage()
      }
    )
}
