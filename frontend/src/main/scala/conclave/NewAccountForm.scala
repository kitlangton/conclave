package conclave

import com.raquo.laminar.api.L._
import components.Component
import conclave.model.{Email, Password}
import zio.interop.laminar.ZioOps

case class NewAccountForm() extends Component {

  val emailVar    = Var("")
  val passwordVar = Var("")

  override def body: HtmlElement =
    div(
      width("600px"),
      maxWidth("600px"),
      margin("0 auto"),
      display.flex,
      flexDirection.column,
      margin("80px"),
      "Create Account",
      div(height("40px")),
      Components.formLabel("EMAIL"),
      div(height("4px")),
      Components.textInput("Email", emailVar),
      Components.formLabel("PASSWORD"),
      div(height("4px")),
      Components.textInput("Password", passwordVar).amend(`type` := "password"),
      button(
        "Create Account",
        onClick --> { _ =>
          Clients.loginService
            .register(
              Email(emailVar.now()),
              Password(passwordVar.now())
            )
            .map { token =>
              println(s"YOU GOT A TOKEN ${token}")
              AccountPersistence.setToken(token.jwtString)
              Router.router.pushState(Page.HomePage)
            }
            .toEventStream
        }
      )
    )
}
