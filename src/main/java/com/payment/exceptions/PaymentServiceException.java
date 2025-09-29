package com.payment.exceptions;

import org.springframework.http.HttpStatus;

public class PaymentServiceException extends Exception {
    String message;
    Integer code;
    HttpStatus httpStatus;
    public PaymentServiceException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;

    }
}
