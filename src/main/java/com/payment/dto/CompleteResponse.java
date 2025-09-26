package com.payment.dto;

import com.payment.entity.PaymentTypes;
import lombok.Data;

@Data
public class CompleteResponse {
    int transactionId;
    long amount;
    String status;
    PaymentTypes type;
}
