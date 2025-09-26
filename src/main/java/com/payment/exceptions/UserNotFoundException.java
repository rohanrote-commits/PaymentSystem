package com.payment.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(long id) {
        super("User not found with id " + id);

    }

    public UserNotFoundException(String string) {
        super(string);

    }
}
