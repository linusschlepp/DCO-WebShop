package de.othr.dco.webshop.exceptions;

public  class WebShopException extends Exception{


    public WebShopException(final String formatMessage, final Object... formatFiller) {
        super(String.format(formatMessage, formatFiller));
    }
}
