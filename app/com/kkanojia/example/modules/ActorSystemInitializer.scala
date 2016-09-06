package com.kkanojia.example.modules

import akka.actor.{ActorSystem, Props}
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import com.google.inject.{Inject, Singleton}
import com.kkanojia.example.actors.{TradeAggregateViewActor, UserManager}

import play.api.Logger


@Singleton
class ActorSystemInitializer @Inject()(system: ActorSystem) {

  Logger.info("Initializing Actor system")

  // Init User Manager
  val userManagerProps = Props(
    new UserManager(UserManager.ID)
  )
  system.actorOf(userManagerProps, UserManager.NAME)

  val cumulativeTradeViewProps = Props(
    new TradeAggregateViewActor(TradeAggregateViewActor.ID)
  )
  system.actorOf(cumulativeTradeViewProps, TradeAggregateViewActor.NAME)

  Logger.info("Initializing Actor system complete")

}
