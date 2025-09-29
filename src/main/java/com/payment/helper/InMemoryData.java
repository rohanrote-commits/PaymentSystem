package com.payment.helper;

import com.payment.Payments.PaymentMethods;
import com.payment.entity.PaymentTypes;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryData {
    public Map<Integer,Integer> otp = new HashMap<>();

    public Map<PaymentTypes, PaymentMethods> payments = new HashMap<>();

}
