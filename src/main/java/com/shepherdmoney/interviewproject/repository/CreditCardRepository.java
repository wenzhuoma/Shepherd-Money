package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Crud repository to store credit cards
 */
@Repository("CreditCardRepo")
public interface CreditCardRepository extends JpaRepository<CreditCard, Integer> {
    /**
     * get creditCard by creditCard number
     */
    Optional<CreditCard> findByNumber(String number);
}
