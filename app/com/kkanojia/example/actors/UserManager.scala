package com.kkanojia.example.actors

import scala.collection._

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.kkanojia.example.utils.exceptions.UserPresentException

object UserManager {

  //Command
  case class RetrieveUser(email: String)

  val ID = "um_id_dd782f9d"
  val NAME = "user_manager"
}

class UserManager(val id: String) extends Actor with ActorLogging {

  import UserActor._
  import UserManager._

  private val usersInSystem = mutable.Map[String, String]() //email -> UUID

  val queries = PersistenceQuery(context.system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
  val src: Source[EventEnvelope, NotUsed] = queries.eventsByTag("user-events", 0L)
  implicit val mat = ActorMaterializer()

  src.runForeach { env =>
    env.event match {
      case UserCreated(user) => {
        usersInSystem(user.email) = user.id
      }
      case _ => println(s"Unknown event $env")
    }}

  override def receive: Receive =
    LoggingReceive {

    case CreateUser(user) =>
      if (usersInSystem.contains(user.email))
        sender() ! UserCreationFailed(UserPresentException)
      else
        getUserActor(user.id) forward CreateUser(user)

    case RetrieveUser(email: String) =>
      usersInSystem.get(email) match {
        case Some(userId) => getUserActor(userId.toString) forward GetUser
        case None => sender() ! UserRetrievalFailure
      }

  }

  def onEvent: Receive = {
    case UserCreated(user) => usersInSystem(user.email) = user.id
  }

  private def getUserActor(userId: String): ActorRef = {
    val name = s"user_$userId"
    context.child(name) match {
      case Some(actorRef) => actorRef
      case None => context.actorOf(Props(new UserActor(userId)), name)
    }
  }

}
