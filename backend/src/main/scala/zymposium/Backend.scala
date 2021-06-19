package zymposium

import zhttp.http._
import zio._
import zio.stream._
import zio.app.DeriveRoutes
import zio.magic._
import zymposium.protocol._
import CustomPicklers._
import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.{RootResolver, ZHttpAdapter}
import caliban.schema.GenericSchema
import zymposium.model.{Account, Event, NewAccount, NewEvent, Rsvp}
import zymposium.protocols.{AccountProtocolLive, EventProtocolLive, LoginProtocolLive, UserEventsProtocolLive}
import zymposium.repositories.{AccountRepository, EventRepository}

import java.time.Instant
import java.util.UUID

/** - HI THERE
  */

object GraphQL {

  /** Queries -> read-only / can be done in parallel
    * Mutations -> add, edit, delete (commands) - sequential
    * Subscriptions -> changes published to clients
    */

  case class Queries(
      @GQLDescription("GET ALL MY EVENTS!")
      allEvents: RIO[Has[EventRepository], List[Event]],
      @GQLDescription("GET ALL MY ACCOUNTS!")
      allAccounts: RIO[Has[AccountRepository], List[Account]],
      @GQLDescription("GET ALL MY RSVPS!")
      allRsvps: RIO[Has[EventRepository], List[Rsvp]]
  )

  val queryResolver = Queries(
    allEvents = EventRepository.allEvents,
    allAccounts = AccountRepository.allAccounts,
    allRsvps = EventRepository.allRsvps
  )

  case class CreateEventArgs(newEvent: NewEvent)
  case class CreateAccountArgs(newAccount: NewAccount)
  case class CreateRsvpArgs(rsvp: Rsvp)

  case class Mutations(
      createEvent: CreateEventArgs => RIO[Has[EventRepository], Event],
      createAccount: CreateAccountArgs => RIO[Has[AccountRepository], Account],
      createRsvp: CreateRsvpArgs => RIO[Has[EventRepository], Unit]
  )

  val mutationResolver = Mutations(
    createEvent = args =>
      EventRepository.save(
        Event(UUID.randomUUID(), args.newEvent.title, args.newEvent.description, Instant.now())
      ),
    createAccount = args => AccountRepository.save(Account(UUID.randomUUID(), args.newAccount.email)),
    createRsvp = args => EventRepository.createRsvp(args.rsvp)
  )

  case class Subscriptions(
      allEventsStream: ZStream[Has[EventRepository], Nothing, Event],
      allAccountsStream: ZStream[Has[AccountRepository], Nothing, Account],
      allRsvpsStream: ZStream[Has[EventRepository], Nothing, Rsvp]
  )

  val subscriptionResolver = Subscriptions(
    allEventsStream = EventRepository.allEventsStream,
    allAccountsStream = AccountRepository.allAccountsStream,
    allRsvpsStream = EventRepository.allRsvpsStream
  )

  val resolver = RootResolver(queryResolver, mutationResolver, subscriptionResolver)

  object schema extends GenericSchema[Has[AccountRepository] with Has[EventRepository]]
  import schema._

  val api = graphQL(resolver)

  def main(args: Array[String]): Unit = println(api.render)

  /** Caliban Process
    * 1. Parses Request
    * 2. Validates Request (all fields exist, other rules from spec)
    * 3. Execute
    *    a. Builds ZQuery
    */

//  def allEvents: Task[List[Event]]
//  def allEventsStream: UStream[Event]
//  def createEvent(newEvent: NewEvent): Task[Event]
//
//  def createAccount(newAccount: NewAccount): Task[Account]
//  def allAccountsStream: UStream[Account]
//
//  def allRsvpsStream: UStream[Rsvp]
//  def createRsvp(rsvp: Rsvp): Task[Unit]
}

object Backend extends App {
  private lazy val eventService      = DeriveRoutes.gen[EventProtocol]
  private lazy val accountService    = DeriveRoutes.gen[AccountProtocol]
  private lazy val userEventsService = DeriveRoutes.gen[UserEventsProtocol]
  private lazy val loginRoutes       = DeriveRoutes.gen[LoginProtocol]

  val graphqlRoutes =
    Http.fromEffect {
      for {
        interpreter <- GraphQL.api.interpreter.orDie
      } yield Http.route[Request] {
        case _ -> Root / "api" / "graphql" =>
          ZHttpAdapter.makeHttpService(interpreter)
        case _ -> Root / "ws" / "graphql" =>
          ZHttpAdapter.makeWebSocketService(interpreter)
      }
    }.flatten

  private lazy val app = graphqlRoutes +++ loginRoutes +++ eventService +++ Authentication.authenticate(
    HttpApp.forbidden("Not allowed!"),
    accountService +++ userEventsService
  )
//
  val program = for {
    port <- system.envOrElse("PORT", "8088").map(_.toInt).orElseSucceed(8088)
    _    <- zhttp.service.Server.start(port, CORS(app))
  } yield ()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program
      .injectCustom(
        QuillContext.live,
        AccountRepository.live,
        EventRepository.live,
        EventProtocolLive.layer,
        LoginProtocolLive.layer,
        AccountProtocolLive.layer,
        UserEventsProtocolLive.layer,
        AppContext.live
      )
      .exitCode
}
