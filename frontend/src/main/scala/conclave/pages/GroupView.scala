package conclave.pages

import com.raquo.laminar.api.L._
import components.Component
import conclave.{Colors, JoinZoomButton}
import conclave.model.{Event, Group}
import conclave.protocol.{AccountProtocol, EventProtocol}
import zio.app.DeriveClient
import zio.interop.laminar.ZioOps
import conclave.protocol.CustomPicklers._
import zio.ZIO

import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

case class GroupView(accountProtocol: AccountProtocol, slug: String) extends Component {
  val eventService = DeriveClient.gen[EventProtocol]

  val $group: EventStream[Group] =
    accountProtocol.group(slug).toEventStream

  val $events =
    $group.flatMap { group =>
      println(s"QUERYING EVENTS FOR GROUP ${group.id}")
      eventService
        .allEvents(group.id)
        .tap(events => ZIO.debug("EVENTS: " + events))
        .toEventStream
    }

  val $nextEvent =
    $events.map(_.headOption)

  override def body: HtmlElement =
    mainSection

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
      div(opacity(0.9), fontSize("18px"), child.text <-- $group.map(_.description))
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
      children <-- $events.map(_.tail).split(_.id) { (_, event, _) =>
        historicEvent(event.name, formatShort(event.time))
      },
      div(
        "NO PAST EVENTS",
        hidden <-- $events.map(_.nonEmpty)
      )
//      historicEvent("Variance and You", "August 10th"),
//      historicEvent("Full Stack Scala", "August 3rd"),
//      historicEvent("Optics from Scratch", "July 25th")
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
        child.text <-- $group.map(_.name.toUpperCase),
        fontSize("32px"),
        fontWeight(300),
        color(Colors.primary)
      ),
      child <-- $nextEvent.collect { case Some(event) => eventInformation(event) },
      JoinZoomButton()
    )

  private def eventInformation(event: Event): Div = {
    val (date, time) = formatInstant(event.time)
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
        event.name,
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
        div(date),
        div(s"at $time")
      )
    )
  }

  // Friday, August 10th at 11:00 AM PST
  def formatInstant(instant: Instant): (String, String) = {
    val date = DateTimeFormatter
      .ofPattern("EEEE, MMMM d")
      .format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))

    val time = DateTimeFormatter
      .ofPattern("h:mm a")
      .format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))

    (date, time)
  }

  // 1/10/20222
  def formatShort(instant: Instant): String =
    DateTimeFormatter
      .ofPattern("M/d/yyyy")
      .format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))

}
