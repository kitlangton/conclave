package zymposium

import com.raquo.laminar.api.L._
import components.Component
import graphql.Client
import graphql.Client._
import caliban.client.laminext._
import io.laminext.syntax.core.thisEvents
import io.laminext.websocket.WebSocket
import zio._
import zymposium.events.EventListing
import zymposium.model.AccountInfo
import zymposium.pages.{AdminPage, UserPage}

object CalibanView {
  private val uri = "http://localhost:8088/api/graphql"
  private val ws  = WebSocket.url("ws://localhost:8088/ws/graphql", "graphql-ws").graphql.build()

  val allEvents          = Queries.allEvents(Event.view).toEventStream(uri)
  val eventsSubscription = Subscriptions.allEventsStream(Event.title).toSubscription(ws)
  val createEvent =
    Mutations.createEvent(NewEventInput("OHHHHHHHHPOSIUM", "cool."))(Event.id).toEventStream(uri)
//  eventsSubscription

  def body: Div = div(
    ws.connect,
    ws.connected --> (_ => ws.init()),
    div(
      div("MOST RECENT EVENT"),
      child.text <-- eventsSubscription.received.collect { case Right(Some(title)) =>
        title
      }
    ),
    hr(),
    button(
      "OH YES", //
      thisEvents(onClick).flatMap(_ => createEvent) --> { event =>
        println("CREATED EVENT RESULT", event)
      }
    ),
    hr(),
    child <-- allEvents.map {
      case Right(Some(events)) =>
        div(
          events.map { event =>
            div(
              div(event.title),
              div(event.description)
            )
          }
        )
      case oops =>
        div(s"ERROR: ${oops}")
    }
  )
}

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
      CalibanView.body
//      child <-- modeVar.signal.map {
//        case Mode.User  => UserPage()
//        case Mode.Admin => AdminPage()
//      }
    )
}
