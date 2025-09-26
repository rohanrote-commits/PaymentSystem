package com.payment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.stereotype.Component;
@Data
@Entity
public class NetBanking {

    @Id
    @Column(nullable = false, unique = true)
    private String username;


    @Column(name = "password",nullable = false,length = 5)
    public String password;

    private long balance;


}
