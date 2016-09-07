package com.kkanojia.example.actors

import scala.collection.immutable.HashSet
import scala.collection.mutable

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Stash}
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.kkanojia.example.actors.TradeActor.{TradeCreated, TradeUpdated}
import com.kkanojia.example.actors.TradeAggregateViewActor.{UnWatchTrades, WatchTrades}
import com.kkanojia.example.models.Trade

object TradeAggregateViewActor {

  val ID = "464788cb-58aa-4dc6-8dce-703a456c238a"
  val NAME = "trade_view_aggregate"

  case object WatchTrades

  case object UnWatchTrades

}

class TradeAggregateViewActor(val id: String) extends Actor {

  protected[this] var watchers = HashSet.empty[ActorRef]
  private val trades = mutable.Map[String, Trade]()

  val queries = PersistenceQuery(context.system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
  val src: Source[EventEnvelope, NotUsed] = queries.eventsByTag("trade-events", 0L)
  implicit val mat = ActorMaterializer()

  src.runForeach { env =>
    env.event match {
      case TradeCreated(userId, trade) =>
        trades(trade.id) = trade
        watchers.foreach(_ ! TradeCreated(userId, trade))

      case TradeUpdated(userId, trade) =>
        trades(trade.id) = trade
        watchers.foreach(_ ! TradeUpdated(userId, trade))

      case _ => println(s"Unknown event $env")
    }
  }

  override def receive: Receive = {

    case WatchTrades =>
      watchers = watchers + sender

    case UnWatchTrades =>
      watchers = watchers - sender

  }

}
