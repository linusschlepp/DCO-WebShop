package de.othr.dco.webshop.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.othr.dco.webshop.actors.BasketActor._
import de.othr.dco.webshop.entities.{Item, Order, Payment, User}
import de.othr.dco.webshop.exceptions.WebShopException


object WebShopActor {

  sealed trait WebShopMessage

  case class PaymentError(payment: Payment, order: Order) extends WebShopMessage

  case class CreateBasket() extends WebShopMessage

  case class PaymentCollected(payment: Payment, User: User) extends WebShopMessage

  case class MakeOrder(user: User) extends WebShopMessage

  case class AddItemToBasket(item: Item, user: User) extends WebShopMessage

  def apply(): Behavior[WebShopMessage] = internalBehavior(null)


  def internalBehavior(basketActor: ActorRef[BasketMessage]): Behavior[WebShopMessage] = Behaviors.setup { context =>
    Behaviors.receiveMessagePartial {
      case MakeOrder(user) =>
        // instantiate default order object
        if(basketActor == null) {
          throw new WebShopException("Basket needs to be created first")
        }
        val newUser: User = new User(user.id, user.name, user.address,  new Order(List(): List[Item], new Payment(0)))
        basketActor ! InitOrder(newUser)
        Behaviors.same
      case AddItemToBasket(item, user) =>
        basketActor ! AddItemToUserBasket(user, item)
        Behaviors.same
      case PaymentCollected(payment, user) =>
        basketActor ! GetAllItemsForUser(user)
        val newPaymentActor  = context.spawn(PaymentActor(), "payment-actor")
        basketActor ! ConductPayment(user, payment, newPaymentActor)
        Behaviors.stopped
      case CreateBasket() =>
        if(basketActor != null) {
          println("Basket was already created!")
          Behaviors.same
        }
        val newBasketActor  = context.spawn(BasketActor(), "basket-actor")
        println("Successfully created basket")
        internalBehavior(newBasketActor)
    }

  }







}