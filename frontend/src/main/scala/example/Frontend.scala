package example

import com.raquo.laminar.api.L._
import example.protocol.{Event, EventService, NewEvent}
import zio._
import zio.app.DeriveClient
import animus._
import example.protocol.CustomPicklers._
import formula.{DeriveForm, Form}

object Frontend {
  val runtime = Runtime.default
  val client  = DeriveClient.gen[EventService]

  val newEventVar  = Var(NewEvent("", ""))
  val allEventsVar = Var(List.empty[Event])

  implicit val newEventForm: Form[NewEvent] =
    DeriveForm.gen[NewEvent]

  def view: Div =
    div(
      h3("IMPORTANT WEBSITE"),
      Form.render(newEventVar),
      button(
        "SAVE EVENT",
        onClick --> { _ =>
          runtime.unsafeRunAsync_(
            client.createEvent(newEventVar.now()) *>
              UIO(reloadEvents) *>
              UIO(newEventVar.set(NewEvent("", "")))
          )
        }
      ),
      onMountCallback { _ => reloadEvents },
      eventsView
    )

  private def reloadEvents: Unit =
    runtime.unsafeRunAsync_(
      client.allEvents.tap { events =>
        UIO(allEventsVar.set(events))
      }
    )

  val eventsView = div(
    children <-- allEventsVar.signal.split(_.id) { (_, _, $event) =>
      div(
        h4(
          child.text <-- $event.map(_.title)
        ),
        div(
          child.text <-- $event.map(_.description)
        ),
        div(
          child.text <-- $event.map(_.time.toString)
        )
      )
    }
  )
}
