package com.payment.dto;

import lombok.Data;

@Data
public class InitiateResponse {
    public long userAccountNo;
    public long transactionID;
    public int otp;
    public long amount;

}
