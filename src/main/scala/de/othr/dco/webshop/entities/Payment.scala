package de.othr.dco.webshop.entities

class Payment(val amount: Double, val paymentMethod: PaymentMethod) {


  def this(amount: Double) = this(amount, PaymentMethod.PAYPAL)


  override def toString = s"Payment($amount, $paymentMethod)"
}
