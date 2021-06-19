package graphql

import caliban.client.FieldBuilder._
import caliban.client._
import caliban.client.__Value._

object Client {

  type ID = String

  type Instant = String

  type Account
  object Account {

    final case class AccountView(
        id: String,
        email: String,
        githubAccessToken: Option[String],
        githubRefreshToken: Option[String]
    )

    type ViewSelection = SelectionBuilder[Account, AccountView]

    def view: ViewSelection = (id ~ email ~ githubAccessToken ~ githubRefreshToken).map {
      case (((id, email), githubAccessToken), githubRefreshToken) =>
        AccountView(id, email, githubAccessToken, githubRefreshToken)
    }

    def id: SelectionBuilder[Account, String]    = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def email: SelectionBuilder[Account, String] = _root_.caliban.client.SelectionBuilder.Field("email", Scalar())
    def githubAccessToken: SelectionBuilder[Account, Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("githubAccessToken", OptionOf(Scalar()))
    def githubRefreshToken: SelectionBuilder[Account, Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("githubRefreshToken", OptionOf(Scalar()))
  }

  type Event
  object Event {

    final case class EventView(id: String, title: String, description: String, time: Instant)

    type ViewSelection = SelectionBuilder[Event, EventView]

    def view: ViewSelection = (id ~ title ~ description ~ time).mapN(EventView)

    def id: SelectionBuilder[Event, String]    = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def title: SelectionBuilder[Event, String] = _root_.caliban.client.SelectionBuilder.Field("title", Scalar())
    def description: SelectionBuilder[Event, String] =
      _root_.caliban.client.SelectionBuilder.Field("description", Scalar())
    def time: SelectionBuilder[Event, Instant] = _root_.caliban.client.SelectionBuilder.Field("time", Scalar())
  }

  type Rsvp
  object Rsvp {

    final case class RsvpView(accountId: String, eventId: String)

    type ViewSelection = SelectionBuilder[Rsvp, RsvpView]

    def view: ViewSelection = (accountId ~ eventId).map { case (accountId, eventId) => RsvpView(accountId, eventId) }

    def accountId: SelectionBuilder[Rsvp, String] = _root_.caliban.client.SelectionBuilder.Field("accountId", Scalar())
    def eventId: SelectionBuilder[Rsvp, String]   = _root_.caliban.client.SelectionBuilder.Field("eventId", Scalar())
  }

  case class NewAccountInput(email: String)
  object NewAccountInput {
    implicit val encoder: ArgEncoder[NewAccountInput] = new ArgEncoder[NewAccountInput] {
      override def encode(value: NewAccountInput): __Value =
        __ObjectValue(List("email" -> implicitly[ArgEncoder[String]].encode(value.email)))
    }
  }
  case class NewEventInput(title: String, description: String)
  object NewEventInput {
    implicit val encoder: ArgEncoder[NewEventInput] = new ArgEncoder[NewEventInput] {
      override def encode(value: NewEventInput): __Value =
        __ObjectValue(
          List(
            "title"       -> implicitly[ArgEncoder[String]].encode(value.title),
            "description" -> implicitly[ArgEncoder[String]].encode(value.description)
          )
        )
    }
  }
  case class RsvpInput(accountId: String, eventId: String)
  object RsvpInput {
    implicit val encoder: ArgEncoder[RsvpInput] = new ArgEncoder[RsvpInput] {
      override def encode(value: RsvpInput): __Value =
        __ObjectValue(
          List(
            "accountId" -> implicitly[ArgEncoder[String]].encode(value.accountId),
            "eventId"   -> implicitly[ArgEncoder[String]].encode(value.eventId)
          )
        )
    }
  }
  type Queries = _root_.caliban.client.Operations.RootQuery
  object Queries {

    /** GET ALL MY EVENTS!
      */
    def allEvents[A](
        innerSelection: SelectionBuilder[Event, A]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("allEvents", OptionOf(ListOf(Obj(innerSelection))))

    /** GET ALL MY ACCOUNTS!
      */
    def allAccounts[A](
        innerSelection: SelectionBuilder[Account, A]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("allAccounts", OptionOf(ListOf(Obj(innerSelection))))

    /** GET ALL MY RSVPS!
      */
    def allRsvps[A](
        innerSelection: SelectionBuilder[Rsvp, A]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("allRsvps", OptionOf(ListOf(Obj(innerSelection))))
  }

  type Mutations = _root_.caliban.client.Operations.RootMutation
  object Mutations {
    def createEvent[A](newEvent: NewEventInput)(innerSelection: SelectionBuilder[Event, A])(implicit
        encoder0: ArgEncoder[NewEventInput]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "createEvent",
        OptionOf(Obj(innerSelection)),
        arguments = List(Argument("newEvent", newEvent, "NewEventInput!")(encoder0))
      )
    def createAccount[A](newAccount: NewAccountInput)(innerSelection: SelectionBuilder[Account, A])(implicit
        encoder0: ArgEncoder[NewAccountInput]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "createAccount",
        OptionOf(Obj(innerSelection)),
        arguments = List(Argument("newAccount", newAccount, "NewAccountInput!")(encoder0))
      )
    def createRsvp(rsvp: RsvpInput)(implicit
        encoder0: ArgEncoder[RsvpInput]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, Option[Unit]] =
      _root_.caliban.client.SelectionBuilder
        .Field("createRsvp", OptionOf(Scalar()), arguments = List(Argument("rsvp", rsvp, "RsvpInput!")(encoder0)))
  }

  type Subscriptions = _root_.caliban.client.Operations.RootSubscription
  object Subscriptions {
    def allEventsStream[A](
        innerSelection: SelectionBuilder[Event, A]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootSubscription, Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field("allEventsStream", OptionOf(Obj(innerSelection)))
    def allAccountsStream[A](
        innerSelection: SelectionBuilder[Account, A]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootSubscription, Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field("allAccountsStream", OptionOf(Obj(innerSelection)))
    def allRsvpsStream[A](
        innerSelection: SelectionBuilder[Rsvp, A]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootSubscription, Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field("allRsvpsStream", OptionOf(Obj(innerSelection)))
  }

}
