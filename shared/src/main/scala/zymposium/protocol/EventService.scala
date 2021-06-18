package zymposium.protocol

import zio._
import zio.stream.UStream

import java.util.UUID

case class Guest(
    id: UUID,
    name: String
)

case class Appearance(
    eventId: UUID,
    personId: UUID
)

trait EventService {
  def createAccount(newAccount: NewAccount): Task[Account]
  def allEvents: Task[List[Event]]
  def allEventsStream: UStream[Event]
  def allAccountsStream: UStream[Account]
  def createEvent(newEvent: NewEvent): Task[Event]

  // RSVP
  def rsvpStream: UStream[Rsvp]
  def createRsvp(rsvp: Rsvp): Task[Unit]
}

case class JwtToken(jwtString: String) extends AnyVal

trait LoginService {
  def login(username: String, password: String): IO[String, JwtToken]
}

// Authed Services
case class AccountInfo(email: String)

trait AccountService {
  def me: UIO[AccountInfo]
}

trait UserEventsService {
  def rsvpedEvents: UIO[List[Rsvp]]

  def rsvp(event: UUID): UIO[Rsvp]
  def removeRsvp(event: UUID): UIO[Unit]
}
