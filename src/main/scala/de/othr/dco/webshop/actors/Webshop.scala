package de.othr.dco.webshop.actors

import akka.actor.typed.ActorSystem
import de.othr.dco.webshop.actors.WebShopActor.{AddItemToBasket, CreateBasket, MakeOrder, PaymentCollected}
import de.othr.dco.webshop.entities.{Address, Item, Payment, User}

import java.util.UUID

object Webshop extends App {


  val system = ActorSystem.create(WebShopActor(), "webshop-actor")


  val user: User = new User(UUID.randomUUID().toString, "LINUS", new Address("Hauptstraße 1", "Regensburg", 12345))
  val user1: User = new User(UUID.randomUUID().toString, "Sebi", new Address("Hauptstraße 15", "Regensburg", 12345))
  val item: Item = new Item("Fancy shoes", 69.0)
  val item1: Item = new Item("Cool sweater", 42.0)
  system ! CreateBasket()
  system ! MakeOrder(user)
  system ! MakeOrder(user1)
  system ! AddItemToBasket(item, user)
  system ! AddItemToBasket(item1, user)
  system ! AddItemToBasket(item, user1)
  system ! PaymentCollected(new Payment(120.0), user1)
  system ! PaymentCollected(new Payment(120.0), user)



}


