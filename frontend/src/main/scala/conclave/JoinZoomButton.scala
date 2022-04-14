package conclave

import animus._
import com.raquo.laminar.api.L._
import components.Component

final case class JoinZoomButton() extends Component {
  val $countdown: Signal[Int] = EventStream
    .periodic(1000)
    .foldLeft(60 * 40 + 3)((time, _) => time - 1)

  val $showZoom = $countdown.map(_ <= 60 * 30)
  val $isActive = $countdown.map(_ <= 60 * 10)
  val $color    = $isActive.map(if (_) Colors.primary else Colors.subtle)

  override def body: HtmlElement =
    div(
      display.flex,
      flexDirection.column,
      alignItems.center,
      div(
        fontSize("14px"),
        child.text <-- $countdown.map { n =>
          if (n <= 0) "LIVE NOW"
          else {
            var seconds = (n % 60).toString
            if (seconds.length == 1) seconds = "0" + seconds
            s"LIVE IN ${n / 60}:$seconds"
          }
        },
        paddingBottom("4px")
      ),
      div(
        display("flex"),
        line,
        alignItems.center,
        div(
          cursor.pointer,
          padding("0 12px"),
          div(
            "JOIN ZOOM",
            Transitions.height($showZoom),
            Transitions.opacity($showZoom)
          )
        ),
        line,
        fontWeight.bold
      ),
      cls("countdown"),
      color <-- $color
    )

  private def line: Div =
    div(
      width <-- $isActive.map(if (_) 32.0 else 0.0).spring.px,
//      width("32px"),
      height("1px"),
      background(Colors.primary)
    )
}
