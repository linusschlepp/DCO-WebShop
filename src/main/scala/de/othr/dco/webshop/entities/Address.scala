package de.othr.dco.webshop.entities

class Address(val street: String, val city: String, val postalCode: Int) {


  override def toString = s"Address($street, $city, $postalCode)"
}
