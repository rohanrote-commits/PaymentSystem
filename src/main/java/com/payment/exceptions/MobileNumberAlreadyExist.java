package com.payment.exceptions;

public class MobileNumberAlreadyExist extends Exception{
    public MobileNumberAlreadyExist(){
        super("Mobile number already exist");
    }
}
