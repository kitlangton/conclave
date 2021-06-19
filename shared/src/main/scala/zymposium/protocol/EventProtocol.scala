package zymposium.protocol

import zio._
import zio.stream.UStream
import zymposium.model._

trait EventProtocol {
  def allEvents: Task[List[Event]]

  def allEventsStream: UStream[Event]
  def createEvent(newEvent: NewEvent): Task[Event]

  def createAccount(newAccount: NewAccount): Task[Account]
  def allAccountsStream: UStream[Account]

  def allRsvpsStream: UStream[Rsvp]
  def createRsvp(rsvp: Rsvp): Task[Unit]
}
