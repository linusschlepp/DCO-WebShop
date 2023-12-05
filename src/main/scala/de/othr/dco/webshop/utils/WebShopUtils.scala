package de.othr.dco.webshop.utils

import akka.actor.typed.ActorRef
import de.othr.dco.webshop.actors.BasketActor.BasketMessage
import de.othr.dco.webshop.entities.User

object WebShopUtils {


  def getUserInMap(map: Map[User, ActorRef[BasketMessage]], user: User): User = map.keySet.filter(u=>u.id==user.id).head

  def getActorRefForUser(map: Map[User, ActorRef[BasketMessage]], user: User): ActorRef[BasketMessage] = map(getUserInMap(map, user))

  def isUserInMap(map: Map[User, ActorRef[BasketMessage]], user: User): Boolean = map.keySet.map(u=>u.id).contains(user.id)


  def buildNewMap(map: Map[User, ActorRef[BasketMessage]], user: User,  newBasketActor: ActorRef[BasketMessage]): Map[User, ActorRef[BasketMessage]] =
      Map.newBuilder.addAll(map).addOne(user, newBasketActor).result()

  def isBasketForUser(map: Map[User, ActorRef[BasketMessage]], user: User): Boolean = map.contains(user)

}
