package de.othr.dco.webshop.actors

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.othr.dco.webshop.actors.BasketActor._
import de.othr.dco.webshop.entities.{Item, Order, Payment, User}
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

  case class PaymentError(payment: Payment, order: Order) extends WebShopMessage

  case class AddUserToMap(user: User, exception: Throwable, basketRef: ActorRef[BasketMessage]) extends WebShopMessage

  case class CreateBasket(user: User) extends WebShopMessage

  case class PaymentCollected(payment: Payment, User: User) extends WebShopMessage

  case class MakeOrder(user: User) extends WebShopMessage

  case class AddItemToBasket(item: Item, user: User) extends WebShopMessage

  def apply(): Behavior[WebShopMessage] = internalBehavior(Map())


  def internalBehavior(map: Map[User, ActorRef[BasketMessage]]): Behavior[WebShopMessage] =
    Behaviors.setup { context =>
      implicit val timeout: Timeout = Timeout(3 seconds)
      Behaviors.receiveMessage {
        case MakeOrder(user) =>
          // instantiate default order object
          if (!WebShopUtils.isBasketForUser(map, user)) {
            throw new WebShopException("Basket needs to be created first")
          }
          val newUser: User = new User(user.id, user.name, user.address, new Order(List(): List[Item], new Payment(0)))
          WebShopUtils.getActorRefForUser(map, user) ! InitOrder(newUser)
          Behaviors.same
        case AddItemToBasket(item, user) =>
          if(!WebShopUtils.isUserInMap(map, user)) {
            throw new WebShopException("Basket needs to be created first");
          }
          val actorRef: ActorRef[BasketMessage] = WebShopUtils.getActorRefForUser(map, user)
          // Check if user is already a key in the map - if yes fetch it - if not simply continue by using the given user
          val newUser: User = if (WebShopUtils.isUserInMap(map, user)) WebShopUtils.getUserInMap(map, user) else user
          context.ask(actorRef, ref => AddItemToUserBasket(newUser, item, ref)) {
            case Success(value) =>
              AddUserToMap(value.user, null, actorRef)
            // Is equivalent to:
            //  case Success(value) =>
            //     AddUserToMap(value.user, null)
            case Failure(ex) => AddUserToMap(user, ex, actorRef)
          }
          Behaviors.same
        case AddUserToMap(user, ex, ref) =>
          if (ex != null) {
            throw new WebShopException("Item can not be added to users: %s basket", ex, user)
          }
          val tempMap = map.-(WebShopUtils.getUserInMap(map, user))
          val newMap = WebShopUtils.buildNewMap(tempMap, user, ref)
          newMap.keySet.foreach(println)
          internalBehavior(newMap)
        case PaymentCollected(payment, user) =>
          WebShopUtils.getActorRefForUser(map, user) ! GetAllItemsForUser(user)
          val newPaymentActor = context.spawn(PaymentActor(), s"payment-actor-${user.id}")
          WebShopUtils.getActorRefForUser(map, user) ! ConductPayment(user, payment, newPaymentActor)
          Behaviors.stopped
        case CreateBasket(user) =>
          if (!WebShopUtils.isBasketForUser(map, user)) {
            println("Basket was already created!")
            Behaviors.same
          }
          val newBasketActor = context.spawn(BasketActor(), s"basket-actor-${user.id}")
          val newMap = WebShopUtils.buildNewMap(map, user, newBasketActor)
          println(s"Successfully created basket for user: $user")
          internalBehavior(newMap)
      }
    }
}