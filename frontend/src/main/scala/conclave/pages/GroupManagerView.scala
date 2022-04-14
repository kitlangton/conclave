package conclave.pages

import com.raquo.laminar.api.L._
import components.Component
import conclave.{Page, Router}
import conclave.model.Group
import conclave.protocol.AccountProtocol
import zio.interop.laminar.ZioOps

case class GroupManagerView(accountProtocol: AccountProtocol) extends Component {

  val groups: EventStream[List[Group]] = accountProtocol.groups.toEventStream

  override def body: HtmlElement =
    div(
      h4("Your Groups"),
      children <-- groups.split(_.slug) { (slug, _, $group) =>
        div(
          margin("20px 0"),
          cursor.pointer,
          onClick --> { _ =>
            Router.router.pushState(Page.GroupPage(slug))
          },
          div(
            child.text <-- $group.map(_.name)
          )
        )
      }
    )

}
