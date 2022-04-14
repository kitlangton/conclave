package conclave

import com.raquo.laminar.api.L._
import conclave.model.Event
import conclave.pages.{GroupManagerView, GroupView}
import conclave.protocol.{AccountProtocol, EventProtocol, LoginProtocol}
import io.laminext.syntax.core._
import zio.app.{ClientConfig, DeriveClient}
import zio.interop.laminar.ZioOps
import conclave.protocol.CustomPicklers._

object Colors {
  val primary    = "#F95A00"
  val background = "#0F0E0E"
  val subtle     = "#737373"
}

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
  val eventService = DeriveClient.gen[EventProtocol]

  val nextEvent: EventStream[Event] =
    eventService.nextEvent.toEventStream
      .collect { case Some(event) => event }

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
            mainSection
          case (Page.GroupManager, Some(token)) =>
            GroupManagerView(DeriveClient.gen[AccountProtocol](ClientConfig(Some(token))))
          case (Page.CreateGroupPage, Some(token)) =>
            NewGroupForm(DeriveClient.gen[AccountProtocol](ClientConfig(Some(token))))
          case (Page.GroupPage(slug), Some(token)) =>
            GroupView(DeriveClient.gen[AccountProtocol](ClientConfig(Some(token))), slug)
          case _ =>
            mainSection
        }
      )
    )

  private def mainSection: Div =
    div(
      maxWidth("600px"),
      margin("0 auto"),
      display("grid"),
      customStyle[String]("grid-template-columns")("1fr 1fr"),
      customStyle[String]("grid-gap")("0 40px"),
      height("100%"),
      upNextView.amend(
        customStyle[String]("grid-column")("1 / 3"),
        customStyle[String]("grid-row")("1 / 3")
      ),
      historyView,
      descriptionView
    )

  private def descriptionView =
    div(
      flex("1"),
      display.flex,
      flexDirection.column,
      padding("12px"),
      div(
        "DESCRIPTION",
        fontWeight.bold,
        color(Colors.subtle),
        fontSize("14px"),
        marginBottom("32px")
      ),
      div(opacity(0.9), fontSize("18px"), "Zymposium is a weekly meetup on Scala, Functional Programming, and ZIO.")
    )

  private def historyView =
    div(
      textAlign.right,
      flex("1"),
      display.flex,
      flexDirection.column,
      padding("12px"),
      div(
        "HISTORY",
        fontWeight.bold,
        color(Colors.subtle),
        fontSize("14px"),
        marginBottom("32px")
      ),
      historicEvent("Variance and You", "August 10th"),
      historicEvent("Full Stack Scala", "August 3rd"),
      historicEvent("Optics from Scratch", "July 25th")
    )

  private def historicEvent(name: String, date: String): Div = {
    val isHovering = Var(false)
    div(
      onMouseOver.mapToStrict(true) --> isHovering,
      onMouseOut.mapToStrict(false) --> isHovering,
      color <-- isHovering.signal.map(if (_) Colors.primary else "white"),
      opacity(0.9),
      cursor.pointer,
      div(name, fontWeight.bold, fontStyle.italic, fontSize("18px")),
      div(height("4px")),
      div(date, fontSize("14px"), color("#C8C8C8")),
      marginBottom("24px")
    )
  }

  private def upNextView: Div =
    div(
      display("flex"),
      flex("1"),
      flexDirection("column"),
      alignItems.center,
      justifyContent.center,
      div(
        "ZYMPOSIUM",
        fontSize("32px"),
        fontWeight(300),
        color(Colors.primary)
      ),
      eventInformation,
      JoinZoomButton()
    )

  private def eventInformation: Div =
    div(
      display("flex"),
      flexDirection("column"),
      justifyContent.center,
      alignItems.center,
      padding("40px 0px"),
      div(
        color(Colors.subtle),
        fontWeight.bold,
        fontSize("14px"),
        "NEXT UP"
      ),
      div(
//        "Full Stack ZIO",
        child.text <-- nextEvent.map(_.title).toSignal("Loading..."),
        padding("8px 0"),
        fontSize("36px"),
        fontWeight.bold,
        fontStyle.italic
      ),
      div(
        display("flex"),
        flexDirection("column"),
        alignItems.center,
        color("#C8C8C8"),
        fontSize("18px"),
        div("Friday, August 10th"),
        div("at 11:00 AM PST")
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
