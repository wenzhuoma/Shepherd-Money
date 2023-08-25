package com.shepherdmoney.interviewproject.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private Instant date;

    private double balance;

    /**
     * creditCard
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;


    /**
     * initialize balanceHistory
     */
    public BalanceHistory(Instant date, double balance, CreditCard creditCard) {
        this.date = date;
        this.balance = balance;
        this.creditCard = creditCard;
    }
}
