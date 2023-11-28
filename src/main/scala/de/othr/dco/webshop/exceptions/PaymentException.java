package de.othr.dco.webshop.exceptions;

public class PaymentException extends WebShopException{

    public PaymentException(String formatMessage, Object... formatFiller) {
        super(formatMessage, formatFiller);
    }
}
