package de.othr.dco.webshop.entities

class User(val id: String, val name: String, val address: Address, val currentOrder: Order) {

  def this(id: String,  name: String, address: Address) = this(id, name, address, new Order())


  override def toString = s"User($id, $name, $address, $currentOrder)"
}
