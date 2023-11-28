package de.othr.dco.webshop.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.othr.dco.webshop.actors.PaymentActor.{CollectPayment, PaymentMessage}
import de.othr.dco.webshop.entities.{Item, Order, Payment, User}
import de.othr.dco.webshop.exceptions.UserNotFoundException

object BasketActor {


  sealed trait BasketMessage

  case class AddItemToUserBasket(user: User, item: Item) extends BasketMessage

  case class InitOrder(user: User) extends BasketMessage

  case class GetAllItemsForUser(user: User) extends BasketMessage

  case class ConductPayment(user: User, payment: Payment, paymentRef: ActorRef[PaymentMessage]) extends BasketMessage

  def apply(): Behavior[BasketMessage] = internalBehavior(Map())

  def internalBehavior(map: Map[String, User]): Behavior[BasketMessage] =  {
    Behaviors.receiveMessagePartial {
      case GetAllItemsForUser(user) =>
      val orderOption: Option[User] = map.get(user.id)
      if(orderOption.isEmpty) {
        throw new UserNotFoundException("User: %s is not yet registered", user)
      }
        // print out all items of specific user
        orderOption.get.currentOrder.listOfItems.foreach(println)
        Behaviors.same
      case AddItemToUserBasket(user, item) =>
        val orderOption: Option[User] = map.get(user.id)
        if (orderOption.isEmpty) {
          throw new UserNotFoundException("User: %s is not yet registered", user)
        }
        val newOrder = if(orderOption.get.currentOrder.listOfItems.isEmpty) new Order(List(item)) else new Order(orderOption.get.currentOrder.listOfItems :+ item)
        val newUser = new User(user.id, user.name, user.address, newOrder)
        val newMap = Map.newBuilder.addAll(map).addOne(user.id, newUser).result()
        println(s"Successfully added $item to the basket of user $user, the order now contains: ${newOrder.listOfItems}")
        internalBehavior(newMap)
      case InitOrder(user) =>
          if(map.contains(user.id)) {
            println(s"User: $user was already initialized")
            Behaviors.same
          }
        val newMap = Map.newBuilder.addAll(map).addOne(user.id, user).result()
        internalBehavior(newMap)
      case ConductPayment(user, payment, newPaymentActor) =>
          val userOption: Option[User] = map.get(user.id)
          if (userOption.isEmpty) {
            throw new UserNotFoundException("User: %s is not yet registered", user)
          }
        val tempUser = userOption.get
        newPaymentActor ! CollectPayment(payment.amount, tempUser.currentOrder.getSum)
        Behaviors.stopped
    }
  }

}
