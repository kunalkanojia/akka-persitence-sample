package com.kkanojia.example.actors

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import com.kkanojia.example.actors.TradeActor.{TradeCreated, TradeUpdated}
import com.kkanojia.example.actors.UserActor.UserCreated

class TaggingEventAdapter extends WriteEventAdapter {

  override def toJournal(event: Any): Any = event match {
    case e: UserCreated => Tagged(event, Set("user-events"))
    case TradeCreated(userId, _) => Tagged(event, Set(userId, "trade-events"))
    case TradeUpdated(userId, _) => Tagged(event, Set(userId, "trade-events"))
    case _ => event
  }

  override def manifest(event: Any): String = ""

}
