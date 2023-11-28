package de.othr.dco.webshop.exceptions;

public class UserNotFoundException extends WebShopException {
    public UserNotFoundException(String formatMessage, Object... formatFiller) {
        super(formatMessage, formatFiller);
    }
}
