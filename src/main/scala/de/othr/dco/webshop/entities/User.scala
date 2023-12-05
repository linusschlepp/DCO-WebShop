package de.othr.dco.webshop.entities

class User(val id: String, val name: String, val address: Address) {



  override def toString = s"User($id, $name, $address)"
}
