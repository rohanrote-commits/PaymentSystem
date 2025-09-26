package com.payment.exceptions;

public class InsufficientAmmountException extends RuntimeException {
    public InsufficientAmmountException(String message) {
        super(message);
    }
}
