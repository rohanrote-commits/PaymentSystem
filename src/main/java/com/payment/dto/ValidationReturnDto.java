package com.payment.dto;

import lombok.Data;

@Data
public class ValidationReturnDto {
    public long userAccountNo;
    public long transactionID;
    public int otp;
    public long amount;

}
