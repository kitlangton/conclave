package conclave.pages

import com.raquo.laminar.api.L._
import components.Component
import conclave.model.Group
import conclave.protocol.AccountProtocol
import zio.interop.laminar.ZioOps

case class GroupView(accountProtocol: AccountProtocol, slug: String) extends Component {

  val groupInfo: EventStream[Group] =
    accountProtocol.group(slug).toEventStream

  override def body: HtmlElement =
    div(
      h1(
        child.text <-- groupInfo.map(_.name)
      ),
      div(
        child.text <-- groupInfo.map(_.description)
      )
    )

}
