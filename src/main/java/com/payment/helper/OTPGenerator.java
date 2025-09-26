package com.payment.helper;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OTPGenerator {
    public  int generate3DigitOTP() {
        Random random = new Random();
        // 100 to 999 inclusive
        return 100 + random.nextInt(900);
    }

}
