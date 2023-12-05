package de.othr.dco.webshop.exceptions;

public class WebShopException extends Exception{


    public WebShopException(final String formatMessage, final Object... formatFiller) {
        super(String.format(formatMessage, formatFiller));
    }

    public WebShopException(final String formatMessage, final Throwable e, final Object... formatFiller) {
        super(String.format(formatMessage, formatFiller), e);
    }
}
