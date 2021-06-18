package zymposium

import com.raquo.laminar.api.L._
import formula.{DeriveTable, Table}
import zio.UIO
import zymposium.Clients.eventService
import zymposium.Frontend.runtime
import zymposium.protocol.{Account, Event, Rsvp}

case class AdminPage() extends Component {
  val allEventsVar   = Var(List.empty[Event])
  val allAccountsVar = Var(List.empty[Account])
  val allRsvpsVar    = Var(List.empty[Rsvp])

  override def body: HtmlElement =
    div(
      onMountCallback { _ =>
        reloadEvents()
        runtime.unsafeRunAsync_ {
          eventService.allEventsStream.tap { event => UIO(allEventsVar.update(event :: _)) }.runDrain
        }
        runtime.unsafeRunAsync_ {
          eventService.allAccountsStream.tap { account => UIO(allAccountsVar.update(account :: _)) }.runDrain
        }
        runtime.unsafeRunAsync_ {
          eventService.rsvpStream.tap { rsvp => UIO(allRsvpsVar.update(rsvp :: _)) }.runDrain
        }
      },
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

  private def reloadEvents(): Unit =
    runtime.unsafeRunAsync_(
      eventService.allEvents.tap { events =>
        UIO(allEventsVar.set(events))
      }
    )

  implicit val accountTable: Table[Account] = DeriveTable.gen[Account]
  implicit val rsvpTable: Table[Rsvp]       = DeriveTable.gen[Rsvp]
  implicit val eventTable: Table[Event]     = DeriveTable.gen[Event]
}
