package zymposium

import com.raquo.laminar.api.L._
import components.Component
import formula.{DeriveForm, Form}
import zymposium.model.NewAccount

case class NewAccountForm(observer: Observer[NewAccount]) extends Component {
  val newAccountVar = Var(NewAccount(""))

  implicit val newAccountForm: Form[NewAccount] =
    DeriveForm.gen[NewAccount]

  override def body: HtmlElement =
    Form
      .render(newAccountVar)
      .amend(
        onSubmit.preventDefault --> { _ =>
          observer.onNext(newAccountVar.now())
          newAccountVar.set(NewAccount(""))
        }
      )
}
