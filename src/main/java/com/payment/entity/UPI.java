package com.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UPI {

    @Id
    public String upiId;

    @Column(length = 5)
    public String password;
    private long balance;
}
