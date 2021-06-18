package zymposium.pages

import com.raquo.laminar.api.L._
import components.Component
import formula.DeriveTable.gen
import formula.Table
import zio.UIO
import zymposium.Clients.eventService
import zymposium.Frontend.runtime
import zymposium.events.NewEventForm
import zymposium.model.{Account, Event, Rsvp}
import zymposium.{NewAccountForm, RsvpForm}

case class AdminPage() extends Component {
  val allEventsVar   = Var(List.empty[Event])
  val allAccountsVar = Var(List.empty[Account])
  val allRsvpsVar    = Var(List.empty[Rsvp])

  override def body: HtmlElement =
    div(
      subscriptions,
      h4("RSVP"),
      RsvpForm(allAccountsVar, allEventsVar),
      Table.render(allRsvpsVar.signal)(identity),
      h4("Accounts"),
      NewAccountForm(Observer { (newAccount) =>
        runtime.unsafeRunAsync_(eventService.createAccount(newAccount))
      }),
      Table.render(allAccountsVar.signal)(_.id),
      div(height("32px")),
      h4("Events"),
      NewEventForm(Observer { (newEvent) =>
        runtime.unsafeRunAsync_(eventService.createEvent(newEvent))
      }),
      Table.render(allEventsVar.signal)(_.id)
    )

  private def subscriptions = {
    onMountCallback { (_: MountContext[HtmlElement]) =>
      runtime.unsafeRunAsync_ {
        eventService.allEventsStream.tap { event => UIO(allEventsVar.update(event :: _)) }.runDrain
      }
      runtime.unsafeRunAsync_ {
        eventService.allAccountsStream.tap { account => UIO(allAccountsVar.update(account :: _)) }.runDrain
      }
      runtime.unsafeRunAsync_ {
        eventService.allRsvpsStream.tap { rsvp => UIO(allRsvpsVar.update(rsvp :: _)) }.runDrain
      }
    }
  }
}
