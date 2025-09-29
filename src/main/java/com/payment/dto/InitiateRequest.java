package com.payment.dto;

import com.payment.entity.PaymentTypes;
import lombok.Data;

@Data
public class InitiateRequest {
    // card
    private Long cardNo;
    private String cvc;
    private String exp;

    // netbanking
    private String username;
    private String password;

    // cod
    private String mobileNo;

    // upi
    private String upiId;
    private String upiPassword;

    private Long amount;
    private PaymentTypes type;
}
