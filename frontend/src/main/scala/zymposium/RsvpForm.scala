package zymposium

import com.raquo.laminar.api.L._
import components.Component
import zymposium.Clients.eventService
import zymposium.model.{Account, Event, Rsvp}

case class RsvpForm(accountsVar: Var[List[Account]], eventsVar: Var[List[Event]]) extends Component {
  val selectedAccount = Var(Option.empty[Account])
  val selectedEvent   = Var(Option.empty[Event])

  override def body: HtmlElement =
    div(
//      div(
//        child.text <-- selectedAccount.signal.map(_.toString)
//      ),
      div(
        select(
          value <-- selectedAccount.signal.map(_.map(_.id.toString).getOrElse("")),
          onInput.mapToValue --> { id =>
            accountsVar.now().find(_.id.toString == id).foreach { account =>
              selectedAccount.set(Some(account))
            }
          },
          option(value(""), "Select Account"),
          children <-- accountsVar.signal.split(_.id) { (id, account, _) =>
            option(
              value(id.toString),
              account.email
            )
          }
        )
      ),
      div(height("8px")),
      div(
        select(
          value <-- selectedEvent.signal.map(_.map(_.id.toString).getOrElse("")),
          onInput.mapToValue --> { id =>
            eventsVar.now().find(_.id.toString == id).foreach { event =>
              selectedEvent.set(Some(event))
            }
          },
          option(value(""), "Select Event"),
          children <-- eventsVar.signal.split(_.id) { (id, event, _) =>
            option(
              value(id.toString),
              event.title
            )
          }
        )
      ),
      button(
        "RSVP",
        onClick --> { _ =>
          (selectedEvent.now(), selectedAccount.now()) match {
            case (Some(event), Some(account)) =>
              Frontend.runtime.unsafeRunAsync_(
                eventService.createRsvp(Rsvp(account.id, event.id))
              )
              selectedEvent.set(None)
              selectedAccount.set(None)
            case _ => ()
          }
        }
      )
    )
}
