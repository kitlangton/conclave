package zymposium.pages

import com.raquo.laminar.api.L._
import components.Component
import zymposium.LoginForm
import zymposium.events.EventListing
import zymposium.model.AccountInfo

case class UserPage() extends Component {
  val accountVar = Var(Option.empty[(AccountInfo, String)])

  override def body: HtmlElement =
    div(
      LoginForm(accountVar),
      child.maybe <-- accountVar.signal.map {
        _.map { case (_, token) =>
          EventListing(token)
        }
      }
    )
}
