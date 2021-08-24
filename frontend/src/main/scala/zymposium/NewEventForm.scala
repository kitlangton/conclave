package zymposium

import com.raquo.laminar.api.L._
import components.Component
import animus._

final case class NewEventForm() extends Component {
  val nameVar        = Var("Zymposium")
  val descriptionVar = Var("")

  val $isValid: Signal[Boolean] =
    nameVar.signal.combineWithFn(descriptionVar.signal) { (name, desc) =>
      name.nonEmpty && desc.nonEmpty
    }

  override def body: HtmlElement =
    div(
      width("600px"),
      maxWidth("600px"),
      margin("0 auto"),
      display.flex,
      flexDirection.column,
      margin("80px"),
      div(
        div("NEW"),
        div("GROUP"),
        fontStyle.italic,
        fontSize("18px"),
        fontWeight.bold,
        color(Colors.subtle),
        marginBottom("40px")
      ),
      formLabel("GROUP NAME"),
      input(
        controlled(
          value <-- nameVar,
          onInput.mapToValue --> nameVar
        ),
        placeholder("GROUP NAME"),
        textTransform.uppercase,
        fontSize("32px"),
        color(Colors.primary),
        marginBottom("32px")
      ),
      formLabel("DESCRIPTION"),
      div(height("4px")),
      textArea(
        controlled(
          value <-- descriptionVar,
          onInput.mapToValue --> descriptionVar
        ),
        placeholder("Group description"),
        marginBottom("18px")
      ),
      createButton
    )

  private def createButton: Div = {
    val $color = $isValid.map(if (_) Colors.primary else Colors.subtle)
    div(
      display.flex,
      position.relative,
      alignItems.center,
      div("CREATE GROUP"),
      div(
        right("6px"),
        background(Colors.primary),
        borderRadius("1px"),
        padding("4px"),
        color(Colors.background),
        position.absolute,
        "ENTER",
        fontSize("12px"),
        opacity <-- $isValid.map(if (_) 0.8 else 0.0).spring
      ),
      cursor <-- $isValid.map(if (_) "pointer" else "auto"),
      disabled <-- $isValid.map(!_),
      cls.toggle("primary-button") <-- $isValid,
      fontSize("14px"),
      color <-- $color,
      padding("8px"),
      border <-- $color.map(color => s"1px solid $color"),
      inContext { el =>
        width <-- $isValid.map(
          if (_) 300.0
          else 101.0
        ).spring.px
      },
      borderRadius("2px")
    )
  }

  private def formLabel(name: String): Div =
    div(
      name,
      fontSize("14px"),
      textTransform.uppercase,
      color(Colors.subtle),
      marginBottom("8px")
    )
}
