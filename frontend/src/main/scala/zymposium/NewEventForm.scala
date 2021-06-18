package zymposium

import com.raquo.laminar.api.L._
import formula.{DeriveForm, Form}
import zymposium.protocol.NewEvent

case class NewEventForm(observer: Observer[NewEvent]) extends Component {
  val newEventVar = Var(NewEvent("", ""))

  implicit val newEventForm: Form[NewEvent] =
    DeriveForm.gen[NewEvent]

  override def body: HtmlElement =
    Form
      .render(newEventVar)
      .amend(
        button(`type`("submit"), visibility.hidden),
        onSubmit.preventDefault --> { _ =>
          observer.onNext(newEventVar.now())
          newEventVar.set(NewEvent("", ""))
        }
      )
}
