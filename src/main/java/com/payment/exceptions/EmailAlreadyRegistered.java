package com.payment.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;


public class EmailAlreadyRegistered extends RuntimeException{

    public EmailAlreadyRegistered(){
        super("Email already registered");
    }
}
