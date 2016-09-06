package com.kkanojia.example.actors

import akka.persistence.PersistentActor
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import com.kkanojia.example.actors.TradeActor._
import com.kkanojia.example.models.Trade

object TradeActor {

  //Commands
  case class CreateTrade(trade: Trade)

  case class UpdateTrade(trade: Trade)

  //Replies
  case class CreateTradeSuccess(trade: Trade)

  case class CreateTradeFailure(cause: Throwable)

  case class UpdateTradeSuccess(trade: Trade)

  case class UpdateTradeFailure(cause: Throwable)

  //Events
  case class TradeCreated(trade: Trade) extends TaggedEvent

  case class TradeUpdated(trade: Trade) extends TaggedEvent

}

class TradeActor(val id: String
) extends PersistentActor {

  override def persistenceId = id

  private var tradeOpt: Option[Trade] = None

  SharedLeveldbJournal.setStore(context.self, context.system)

  override def receiveCommand: Receive = {

    case CreateTrade(trade) =>
      persist(TradeCreated(trade)) {
       evt => sender() ! CreateTradeSuccess(trade)
      }

    case UpdateTrade(trade) =>
      persist(TradeUpdated(trade)) {
        evt => sender() ! UpdateTradeSuccess(trade)
      }
  }

  override def receiveRecover: Receive = {
    case TradeCreated(trade) =>
      tradeOpt = Some(trade)

    case TradeUpdated(trade) =>
      tradeOpt = Some(trade)
  }
}
