package conclave

import com.raquo.laminar.api.L._
import conclave.model.Event
import conclave.pages.{GroupManagerView, GroupView}
import conclave.protocol.{AccountProtocol, EventProtocol, LoginProtocol}
import io.laminext.syntax.core._
import zio.app.{ClientConfig, DeriveClient}
import zio.interop.laminar.ZioOps
import conclave.protocol.CustomPicklers._

object Clients {
  val loginService: LoginProtocol = DeriveClient.gen[LoginProtocol]
}

/** - Group Management
  *   - List All
  *   - New Group
  *   - Edit Group
  * - Accounts
  *    - Register
  *    - Log In
  *    - Sign Out
  *  - Public Groups
  *     - View Groups
  *   - Group
  *     - Next Event
  *     - Previous Events
  *     - RSVP to single/all
  */

object Frontend {

  def view: Div =
    div(
      display("flex"),
      height("100%"),
      flexDirection("column"),
      title,
      div(
        margin("0 auto"),
        child <-- Router.router.$currentPage.withCurrentValueOf(AccountPersistence.$userToken).map {
          case (Page.SignUp, _) =>
            NewAccountForm()
          case (Page.Login, _) =>
            NewAccountForm()
          case (Page.HomePage, _) =>
            div("Hello")
          case (Page.GroupManager, Some(token)) =>
            GroupManagerView(DeriveClient.gen[AccountProtocol](ClientConfig(Some(token))))
          case (Page.CreateGroupPage, Some(token)) =>
            NewGroupForm(DeriveClient.gen[AccountProtocol](ClientConfig(Some(token))))
          case (Page.GroupPage(slug), Some(token)) =>
            GroupView(DeriveClient.gen[AccountProtocol](ClientConfig(Some(token))), slug)
          case _ =>
            div("NOT FOUND")
        }
      )
    )

  private def title: Div =
    div(
      position("sticky"),
      top("0px"),
      padding("8px"),
      fontSize("18px"),
      div(
        display.flex,
        width("100%"),
        justifyContent.spaceBetween,
        div("CONCLAVE", cursor.pointer, onClick --> { _ => Router.router.pushState(Page.HomePage) }),
        div(
          display.flex,
          div(
            hidden <-- AccountPersistence.$signedIn.map(!_),
            "NEW GROUP",
            fontWeight.normal,
            color(Colors.subtle),
            paddingRight("20px"),
            cursor.pointer,
            onClick --> { _ =>
              Router.router.pushState(Page.CreateGroupPage)
            }
          ),
          div(
            hidden <-- AccountPersistence.$signedIn.map(!_),
            "LOGOUT",
            fontWeight.normal,
            color(Colors.subtle),
            paddingRight("20px"),
            cursor.pointer,
            onClick --> { _ =>
              AccountPersistence.logout()
            }
          ),
          div(
            cursor.pointer,
            child.text <-- AccountPersistence.$accountInfo.map(_.map(_.email.string.toUpperCase).getOrElse("LOGIN")),
            thisEvents(onClick).sample(AccountPersistence.$accountInfo) --> {
              case Some(_) =>
                Router.router.pushState(Page.HomePage)
              case None =>
                Router.router.pushState(Page.Login)
            },
            color(Colors.subtle)
          )
        )
      ),
      fontWeight.bold,
      color(Colors.primary)
    )
}
