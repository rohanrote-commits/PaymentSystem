package com.payment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Card {



    @Id
    @Column(nullable = false, unique = true,length = 11)
    private Long cardNumber;


    @Column(name = "exp")
    private String exp;

    @Column(name = "cvc")
    private String cvc;

    private long balance;

}
