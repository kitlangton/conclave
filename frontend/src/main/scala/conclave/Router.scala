package conclave

import com.raquo.laminar.api.L._
import com.raquo.waypoint._
import scala.scalajs.js
import upickle.default._

sealed trait Page extends Product with Serializable

object Page {
  case object SignUp          extends Page
  case object Login           extends Page
  case object HomePage        extends Page
  case object GroupManager    extends Page
  case object CreateGroupPage extends Page

  final case class GroupPage(slug: String) extends Page

  implicit val signupRW: ReadWriter[SignUp.type]                   = macroRW[SignUp.type]
  implicit val loginRW: ReadWriter[Login.type]                     = macroRW[Login.type]
  implicit val homePageRW: ReadWriter[HomePage.type]               = macroRW[HomePage.type]
  implicit val groupManagerRW: ReadWriter[GroupManager.type]       = macroRW[GroupManager.type]
  implicit val createGroupPageRW: ReadWriter[CreateGroupPage.type] = macroRW[CreateGroupPage.type]
  implicit val groupPageRW: ReadWriter[GroupPage]                  = macroRW[GroupPage]
  implicit val rw: ReadWriter[Page]                                = macroRW
}

object Router {
  val homeRoute         = Route.static(Page.HomePage, root / endOfSegments)
  val signUpRoute       = Route.static(Page.SignUp, root / "signup" / endOfSegments)
  val loginRoute        = Route.static(Page.Login, root / "login" / endOfSegments)
  val groupManagerRoute = Route.static(Page.GroupManager, root / "groups" / endOfSegments)
  val createGroupRoute =
    Route.static(Page.CreateGroupPage, root / "groups" / "new" / endOfSegments)
  val groupRoute = Route[Page.GroupPage, String](
    _.slug,
    slug => Page.GroupPage(slug),
    root / "groups" / segment[String] / endOfSegments
  )

  val router = new Router[Page](
    routes = List(homeRoute, signUpRoute, loginRoute, groupManagerRoute, createGroupRoute, groupRoute),
    getPageTitle = {
      case Page.HomePage        => "CONCLAVE"
      case Page.SignUp          => "CONCLAVE — Sign Up"
      case Page.Login           => "CONCLAVE — Login"
      case Page.GroupManager    => "CONCLAVE — Groups"
      case Page.CreateGroupPage => "CONCLAVE — Create Group"
      case Page.GroupPage(slug) => s"CONCLAVE — $slug"
    },
    serializePage = { page =>
      write(page)
    },
    deserializePage = { pageStr =>
      val result = read[Page](pageStr)
      if (js.isUndefined(result) || result == null) Page.HomePage
      else result
    }
  )(
    $popStateEvent = windowEvents.onPopState, // this is how Waypoint avoids an explicit dependency on Laminar
    owner = unsafeWindowOwner                 // this router will live as long as the window
  )
}
