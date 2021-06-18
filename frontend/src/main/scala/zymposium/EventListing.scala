package zymposium

import animus._
import com.raquo.laminar.api.L._
import zio.app.DeriveClient
import zio.stream.ZStream
import zio.{Chunk, UIO}
import zymposium.LaminarZioSyntax._
import zymposium.protocol.{Event, Rsvp, UserEventsService}

case class EventListing(token: String) extends Component {
  private val client = DeriveClient.gen[UserEventsService](Some(token))

  val rsvpVar: Var[List[Rsvp]] = Var(List.empty)

  val $events: Signal[List[Event]] =
    (ZStream.fromEffect(Clients.eventService.allEvents.map(Chunk.fromIterable(_))).flattenChunks.toEventStream ++
      Clients.eventService.allEventsStream.toEventStream).foldLeft[List[Event]](List.empty)(_.prepended(_))

  override def body: HtmlElement = div(
    "Events",
    client.rsvpedEvents.toEventStream --> rsvpVar,
    children <-- $events.splitTransition(_.id) { (_, event, _, t) =>
      val $isAttending = rsvpVar.signal.map(_.exists(_.eventId == event.id))

      div(
        t.height,
        t.opacity,
        cls("event"),
        div(cls("event-title"), event.title),
        div(cls("event-description"), event.description),
        div(cls("event-time"), event.time.toString),
        button(
          composeEvents(onClick)(_.sample($isAttending)) --> { isAttending =>
            if (isAttending)
              client
                .removeRsvp(event.id)
                .tap { rsvp => UIO(rsvpVar.update(_.filterNot(_.eventId == event.id))) }
                .runAsync()
            else
              client
                .rsvp(event.id)
                .tap { rsvp => UIO(rsvpVar.update(rsvp :: _)) }
                .runAsync()
          },
          child.text <-- $isAttending.map { if (_) "Attending" else "Rsvp" }
        )
      )
    }
  )
}
