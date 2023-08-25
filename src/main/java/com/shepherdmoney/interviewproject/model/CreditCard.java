package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    /**
     * Credit card's owner
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    /**
     * card's balance histories
     */
    @OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL)
    @OrderBy("date DESC")
    private List<BalanceHistory> balanceHistories;
}
