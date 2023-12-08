package de.othr.dco.webshop.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.othr.dco.webshop.actors.PaymentActor.{CollectPayment, PaymentMessage}
import de.othr.dco.webshop.entities.{Item, Payment, User}

object BasketActor {


  sealed trait BasketMessage

  case class AddItemToUserBasket(user: User, item: Item) extends BasketMessage


  case class GetAllItemsForUser(replyTo: ActorRef[BasketMessage]) extends BasketMessage

  case class Response(itemList: List[Item]) extends BasketMessage

  def apply(): Behavior[BasketMessage] = internalBehavior(List())

  def internalBehavior(itemList: List[Item]): Behaviors.Receive[BasketMessage] = {
    Behaviors.receiveMessage {
      case GetAllItemsForUser(basketRef) =>
        println(s"Returning the following items: $itemList")
        basketRef ! Response(itemList)
        Behaviors.same
      case AddItemToUserBasket(user, item) =>
        val newList = itemList :+ item
        println(s"Successfully added $item to the basket of user $user, the order now contains: ${newList}")
        internalBehavior(newList)
    }
  }
}
