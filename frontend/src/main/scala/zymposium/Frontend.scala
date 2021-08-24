package zymposium

import com.raquo.laminar.api.L._
import animus._
import components.Component
import zio.Runtime
import zio.app.DeriveClient
import zio.interop.laminar.ZioOps
import zymposium.model.Event
import zymposium.protocol.EventProtocol
import zymposium.protocol.CustomPicklers._

object Colors {
  val primary    = "#F95A00"
  val background = "#0F0E0E"
  val subtle     = "#737373"
}

object Frontend {
  val runtime = Runtime.default

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
        NewEventForm()
      )
//      mainSection
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
        div("CONCLAVE", cursor.pointer),
        div("KIT", cursor.pointer, fontWeight.normal, color(Colors.subtle))
      ),
      fontWeight.bold,
      color(Colors.primary)
    )
}
