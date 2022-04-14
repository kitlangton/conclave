package conclave

import com.raquo.laminar.api.L._
import components.Component
import animus._
import conclave.protocol.AccountProtocol
import zio.interop.laminar.ZioOps

final case class NewGroupForm(accountProtocol: AccountProtocol) extends Component {
  val nameVar        = Var("")
  val descriptionVar = Var("")

  val $isValid: Signal[Boolean] =
    nameVar.signal.combineWithFn(descriptionVar.signal) { (name, desc) =>
      name.nonEmpty && desc.length >= 10
    }

  override def body: HtmlElement =
    div(
      windowEvents.onKeyDown --> { key =>
        key.key match {
          case "Enter" if key.metaKey =>
            accountProtocol
              .createGroup(nameVar.now(), descriptionVar.now())
              .map { group =>
                println(s"GROUP ${group}")
                Router.router.pushState(Page.GroupManager)
                group
              }
              .toEventStream
          case _ => ()
        }
      },
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
      Components.formLabel("GROUP NAME"),
      input(
        controlled(
          value <-- nameVar,
          onInput.mapToValue --> nameVar
        ),
        autoFocus(true),
        placeholder("GROUP NAME"),
        textTransform.uppercase,
        fontSize("32px"),
        color(Colors.primary),
        marginBottom("32px")
      ),
      Components.formLabel("DESCRIPTION"),
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

  val isPressingCommand  = Var(false)
  val $isPressingCommand = isPressingCommand.signal

  private def createButton: Div = {
    val $color = $isValid.map(if (_) Colors.primary else Colors.subtle)
    div(
      windowEvents.onKeyDown.map(_.metaKey) --> isPressingCommand,
      windowEvents.onKeyUp.map(_.metaKey) --> isPressingCommand,
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
        display.flex,
        div("⌘", paddingRight("4px")),
        div("ENTER"),
        fontSize("12px"),
        opacity <-- $isValid.map(if (_) 0.8 else 0.0).spring
      ),
      cursor <-- $isValid.map(if (_) "pointer" else "auto"),
      disabled <-- $isValid.map(!_),
      cls("primary-button"),
      cls.toggle("is-valid") <-- $isValid,
      cls.toggle("is-hovered") <-- $isPressingCommand.combineWithFn($isValid)(_ && _),
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

}

object Components {
  def formLabel(name: String): Div =
    div(
      name,
      fontSize("14px"),
      textTransform.uppercase,
      color(Colors.subtle),
      marginBottom("8px")
    )

  def textInput[A](name: String, variable: Var[String]): Input =
    input(
      controlled(
        value <-- variable,
        onInput.mapToValue --> variable
      ),
      autoFocus(true),
      placeholder(name.toUpperCase),
      textTransform.uppercase,
      fontSize("32px"),
      color(Colors.primary),
      marginBottom("32px")
    )

}
