package de.othr.dco.webshop.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import de.othr.dco.webshop.exceptions.PaymentException

object PaymentActor {

  sealed trait PaymentMessage


  case class CollectPayment(paymentAmount: Double, orderCost: Double) extends PaymentMessage


  def apply(): Behavior[PaymentMessage] = internalBehavior()


  def internalBehavior(): Behavior[PaymentMessage] = Behaviors.setup { context =>
    Behaviors.receiveMessagePartial {
      case CollectPayment(paymentAmount, orderCost) =>
        if (paymentAmount < orderCost) {
          throw new PaymentException("The Payment-amount of %s is not enough for order-amount: %s. You are missing %s. Order process is aborted!", paymentAmount, orderCost, (paymentAmount-orderCost).abs)
        }
      println(s"Order-process was successful your change is ${paymentAmount-orderCost}")
      Behaviors.stopped
    }

  }












}
