package com.kkanojia.example.actors

import akka.actor.Props
import akka.persistence.PersistentActor
import com.kkanojia.example.models.User
import com.kkanojia.example.utils.exceptions.UserPresentException

object UserActor {

  //Command
  case class CreateUser(user: User)
  case object GetUser

  //Response
  case class UserCreationSuccess(user: User)
  case class UserCreationFailed(cause: Throwable)
  case class UserRetrievalSuccess(user: User)
  case object UserRetrievalFailure

  //Event
  case class UserCreated(user: User) extends TaggedEvent

}

/**
 * Event sourced actor to persist user events to event log
 *
 */
class UserActor(override val persistenceId: String) extends PersistentActor {

  import UserActor._

  private var userOpt: Option[User] = None

  override def receiveCommand: Receive = {

    case CreateUser(user) if userOpt.isDefined =>
      sender() ! UserCreationFailed(UserPresentException)

    case CreateUser(user) =>
      persist(UserCreated(user)) {
        event =>
          updateState(user)
          sender() ! UserCreationSuccess(user)
      }

    case GetUser =>
      userOpt match {
        case Some(user) => sender() ! UserRetrievalSuccess(user)
        case None => sender() ! UserRetrievalFailure
      }
  }

  override def receiveRecover: Receive = {
    case UserCreated(user) =>
      updateState(user)
  }

  private def updateState(user: User): Unit = {
    userOpt = Some(user)
    val name = s"tm_${user.id}"
    context.actorOf(Props(new TradeManager(name, user.id)), name)
  }
}
