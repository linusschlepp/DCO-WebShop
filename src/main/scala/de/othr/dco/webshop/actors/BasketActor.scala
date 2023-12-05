package de.othr.dco.webshop.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.othr.dco.webshop.actors.PaymentActor.{CollectPayment, PaymentMessage}
import de.othr.dco.webshop.entities.{Item, Payment, User}

object BasketActor {


  sealed trait BasketMessage

  case class AddItemToUserBasket(user: User, item: Item) extends BasketMessage

  case class InitOrder(user: User) extends BasketMessage

  case class GetAllItemsForUser() extends BasketMessage

  case class ConductPayment(user: User, payment: Payment, paymentRef: ActorRef[PaymentMessage]) extends BasketMessage

  // ToDO: Maybe response will be need in the future?
  case class Response(user: User)

  def apply(): Behavior[BasketMessage] = internalBehavior(List())

  def internalBehavior(itemList: List[Item]): Behaviors.Receive[BasketMessage] = {
    Behaviors.receiveMessage {
      case GetAllItemsForUser() =>
        // ToDo: Either handle payment action in the BasketActor or return list of items to WebShopActor
        itemList.foreach(println)
        Behaviors.same
      case AddItemToUserBasket(user, item) =>
        val newList = itemList :+ item
        println(s"Successfully added $item to the basket of user $user, the order now contains: ${newList}")
        internalBehavior(newList)
      case ConductPayment(user, payment, newPaymentActor) =>
        // TODO: Handle payment action here?
        Behaviors.same
    }
  }
}
