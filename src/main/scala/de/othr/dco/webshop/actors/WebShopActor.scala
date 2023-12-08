package de.othr.dco.webshop.actors

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.othr.dco.webshop.actors.BasketActor._
import de.othr.dco.webshop.entities.{Item, Payment, User}
import de.othr.dco.webshop.exceptions.{PaymentException, WebShopException}
import akka.util.Timeout
import de.othr.dco.webshop.actors.PaymentActor.{CollectPayment, PaymentMessage}
import de.othr.dco.webshop.utils
import de.othr.dco.webshop.utils.WebShopUtils

import scala.concurrent.Future
import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.language.postfixOps
import scala.util.{Failure, Success}


object WebShopActor {

  sealed trait WebShopMessage

  case class CreateBasket(user: User) extends WebShopMessage

  case class MakeOrder(payment: Payment, user: User) extends WebShopMessage

  case class AdaptedItemListResponse(payment: Payment, itemList: List[Item]) extends WebShopMessage

  case class AddItemToBasket(item: Item, user: User) extends WebShopMessage

  case class Init() extends WebShopMessage

  def apply(): Behavior[WebShopMessage] = internalBehavior(Map(),null)


  def internalBehavior(map: Map[String, ActorRef[BasketMessage]], paymentActor: ActorRef[PaymentMessage]): Behavior[WebShopMessage] =
    Behaviors.setup { context =>
      // Timeout wont be needed for now
      implicit val timeout: Timeout = Timeout(3 seconds)
      Behaviors.receiveMessage {
        case Init() =>
          val pActor = context.spawn(PaymentActor(), "payment-actor")
          internalBehavior(map, pActor)
        case AddItemToBasket(item, user) =>
          // if user is not in basket throw exception
          if(!WebShopUtils.isUserInMap(map, user.id)) {
            throw new WebShopException(s"Basket needs to be created first for User: $user");
          }
          val actorRef: ActorRef[BasketMessage] = WebShopUtils.getActorRefForUser(map, user.id)
          actorRef ! AddItemToUserBasket(user, item)
          Behaviors.same
        case MakeOrder(payment, user) =>
          if(!WebShopUtils.isUserInMap(map, user.id)) {
            throw new WebShopException("Basket needs to be created first for User: %s", user)
          }
          if(paymentActor == null) {
          throw new PaymentException("Payment needs to be initialized first")
        }
          val actorRef: ActorRef[BasketMessage] = WebShopUtils.getActorRefForUser(map, user.id)
          context.ask(actorRef, actorRef => GetAllItemsForUser(actorRef)) {
            case Success(BasketActor.Response(itemList)) =>
              AdaptedItemListResponse(payment, itemList)
            case Failure(ex) => throw new PaymentException("Payment-process failed", ex)
          }
          Behaviors.same
        case AdaptedItemListResponse(payment, itemList) =>
          // Calculate sum of items from basketActor
          val totalPrice = itemList.map(p => p.price).sum
          paymentActor ! CollectPayment(payment.amount, totalPrice)
          Behaviors.same
        case CreateBasket(user) =>
          if (WebShopUtils.isBasketForUser(map, user.id)) {
            println(s"Basket was already created for $user")
            Behaviors.same
          }
          val newBasketActor = context.spawn(BasketActor(), s"basket-actor-${user.id}")
          val newMap = WebShopUtils.buildNewMap(map, user.id, newBasketActor)
          println(s"Successfully created basket for user: $user")
          internalBehavior(newMap, paymentActor)
      }
    }
}