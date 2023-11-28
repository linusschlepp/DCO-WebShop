package de.othr.dco.webshop.entities

class Order(val listOfItems: List[Item], val payment: Payment) {


  def this() = this(List(), null);


  def this(list: List[Item]) = this(list, null)

  def retEnum(): PaymentMethod = PaymentMethod.PAYPAL;



  def getSum: Double = this.listOfItems.map(i=>i.price).sum



  override def toString = s"Order($listOfItems, $payment)"
}
