package com.payment.dto;

import com.payment.Payments.PaymentMethods;
import com.payment.entity.PaymentTypes;
import lombok.Data;

@Data
public class CompleteRequest {
    PaymentTypes type;
    PaymentMethods paymentMethod;
    int transactionId;
    int otp;

    boolean proceed;
}
