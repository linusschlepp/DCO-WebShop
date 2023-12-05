package de.othr.dco.webshop.actors

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.othr.dco.webshop.actors.BasketActor._
import de.othr.dco.webshop.entities.{Item, Payment, User}
import de.othr.dco.webshop.exceptions.WebShopException
import akka.util.Timeout
import de.othr.dco.webshop.utils
import de.othr.dco.webshop.utils.WebShopUtils

import scala.concurrent.Future
import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.language.postfixOps
import scala.util.{Failure, Success}


object WebShopActor {

  sealed trait WebShopMessage

 // case class PaymentError(payment: Payment, order: Order) extends WebShopMessage


  case class CreateBasket(user: User) extends WebShopMessage

  case class PaymentCollected(payment: Payment, User: User) extends WebShopMessage

  case class MakeOrder(user: User) extends WebShopMessage

  case class AddItemToBasket(item: Item, user: User) extends WebShopMessage

  def apply(): Behavior[WebShopMessage] = internalBehavior(Map())


  def internalBehavior(map: Map[String, ActorRef[BasketMessage]]): Behavior[WebShopMessage] =
    Behaviors.setup { context =>
      // Timeout wont be needed for now
    //  implicit val timeout: Timeout = Timeout(3 seconds)
      Behaviors.receiveMessage {
        case AddItemToBasket(item, user) =>
          // if user is not in basket throw exception
          if(!WebShopUtils.isUserInMap(map, user.id)) {
            throw new WebShopException(s"Basket needs to be created first for User: $user");
          }
          val actorRef: ActorRef[BasketMessage] = WebShopUtils.getActorRefForUser(map, user.id)
          actorRef ! AddItemToUserBasket(user, item)
          Behaviors.same
        case PaymentCollected(payment, user) =>
          // TODO: Make BasketActor.GetAllItemsForUser make return all items (or sum of price) or
          //  call BasketActor ! ConductPayment and let the BasketActor handle the payment-logic??
          Behaviors.same
        case CreateBasket(user) =>
          if (WebShopUtils.isBasketForUser(map, user.id)) {
            println(s"Basket was already created for $user")
            Behaviors.same
          }
          val newBasketActor = context.spawn(BasketActor(), s"basket-actor-${user.id}")
          val newMap = WebShopUtils.buildNewMap(map, user.id, newBasketActor)
          println(s"Successfully created basket for user: $user")
          internalBehavior(newMap)
      }
    }
}