package de.othr.dco.webshop.utils

import akka.actor.typed.ActorRef
import de.othr.dco.webshop.actors.BasketActor.BasketMessage
import de.othr.dco.webshop.entities.User

object WebShopUtils {


 // def getUserInMap(map: Map[String, ActorRef[BasketMessage]], uId: String): User = map.keySet.filter(u=>u.id==uId).head

  def getActorRefForUser(map: Map[String, ActorRef[BasketMessage]], uId: String): ActorRef[BasketMessage] = map(uId)

  def isUserInMap(map: Map[String, ActorRef[BasketMessage]], uId: String): Boolean = map.contains(uId)


  def buildNewMap(map: Map[String, ActorRef[BasketMessage]], uId: String,  newBasketActor: ActorRef[BasketMessage]): Map[String, ActorRef[BasketMessage]] =
      Map.newBuilder.addAll(map).addOne(uId, newBasketActor).result()

  def isBasketForUser(map: Map[String, ActorRef[BasketMessage]], uId: String): Boolean = map.contains(uId)

}
