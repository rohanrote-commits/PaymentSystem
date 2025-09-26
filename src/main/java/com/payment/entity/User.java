package com.payment.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", length = 10)
    private Long accountId;


    @Column(name = "name")
    public String name;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(length = 10,nullable = false,unique = true)
    public String mobileNumber;


}
