package com.kkanojia.example.actors

import scala.collection.mutable

import akka.NotUsed
import akka.actor.{Actor, ActorRef, Props}
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.kkanojia.example.models.Trade

object TradeManager {

  //Command
  case object RetrieveTrades

  case class FindTrade(tradeId: String)

  //Replies
  case class RetrieveTradesSuccess(trades: Seq[Trade])

  case class FindTradeSuccess(tradeOpt: Option[Trade])

}

class TradeManager(val id: String, userId: String) extends Actor {

  import TradeActor._
  import TradeManager._

  private val userTrades = mutable.Map[String, Trade]()

  val queries = PersistenceQuery(context.system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  val src: Source[EventEnvelope, NotUsed] = queries.eventsByTag(userId, 0L)

  implicit val mat = ActorMaterializer()
  src.runForeach { env =>
    env.event match {
      case TradeCreated(_, trade) =>
        userTrades(trade.id) = trade

      case TradeUpdated(_, trade) =>
        userTrades(trade.id) = trade

      case _ => println(s"Unknown event $env")
    }}

  override def receive: Receive = {

    case CreateTrade(trade) =>
      getTradeActor(trade.id) forward CreateTrade(trade)

    case RetrieveTrades =>
      sender() ! RetrieveTradesSuccess(userTrades.values.toSeq)

    case FindTrade(tradeId: String) =>
      sender() ! FindTradeSuccess(userTrades.get(tradeId))

    case UpdateTrade(trade) =>
      getTradeActor(trade.id) forward UpdateTrade(trade)

  }

  private def getTradeActor(tradeId: String): ActorRef = {
    val name = s"trade_$tradeId"
    context.child(name) match {
      case Some(actorRef) => actorRef
      case None => context.actorOf(Props(new TradeActor(tradeId, userId)), name)
    }
  }
}
