package de.othr.dco.webshop.entities

class Item(val name: String, val price: Double) {

  override def toString = s"Item($name, $price)"
}
