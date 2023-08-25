package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    /**
     * creditCard repository
     */
    @Autowired
    CreditCardRepository creditCardRepository;

    /**
     * user repository
     */
    @Autowired
    UserRepository userRepository;

    /**
     * add credit card to user API
     * @param payload
     * @return response
     */
    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        //get user by user id in the database
        Optional<User> user = userRepository.findById(payload.getUserId());
        //return 400 Bad Request if user with userId does not exist
        if(user.isEmpty()){
            return new ResponseEntity<>(payload.getUserId(), HttpStatus.BAD_REQUEST);
        }

        // Create a credit card entity with information given in the payload
        CreditCard creditCard = new CreditCard();
        creditCard.setOwner(user.get());
        creditCard.setIssuanceBank(payload.getCardIssuanceBank());
        creditCard.setNumber(payload.getCardNumber());
        //credit card is successfully associated with the user in the database
        creditCardRepository.save(creditCard);

        //return 200 OK with the credit card id
         return ResponseEntity.ok(creditCard.getId());

    }

    /**
     * get all cards by user id API
     * @param userId
     * @return credit card list
     */
    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        //get user by user id in the database
        Optional<User> user = userRepository.findById(userId);
        //return 400 Bad Request if user with userId does not exist
        if(user.isEmpty()){
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        //a list of all credit card associated with the given userId
        List<CreditCard> creditCradsList = user.get().getCreditCards();
        List<CreditCardView> creditCardViews = creditCradsList.stream().map(creditCard -> new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber())).toList();

        //return 200 OK with the list of user's credit cards
        return ResponseEntity.ok(creditCardViews);
    }

    /**
     * get user id of credit card
     * @param creditCardNumber
     * @return response
     */
    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        //get credit card by card number in the database
        Optional<CreditCard> creditCard = creditCardRepository.findByNumber(creditCardNumber);
        if (creditCard.isEmpty()) {
            //no user associtated with the credit card, return 400 Bad Request
            return ResponseEntity.badRequest().build();
        }else{
            //there is a user associated with the credit card, return the user id in a 200 OK response
            return ResponseEntity.ok(creditCard.get().getOwner().getId());
        }
    }

    /**
     *update balance of credit card
     * @param payload
     * @return
     */

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Void> updateBalancesOfCreditCard(@RequestBody UpdateBalancePayload[] payload) {
        try {
            //sort the payload by transaction time
            Arrays.sort(payload, Comparator.comparing(UpdateBalancePayload::getTransactionTime));
            // set updateBalancePayload transactionTimes to start of day

            for (UpdateBalancePayload updateBalance : payload) {
                // set updateBalancePayload transactionTimes to start of day
                updateBalance.setTransactionTime(updateBalance.getTransactionTime().truncatedTo(ChronoUnit.DAYS));
                //get the current creditCart by credit card Number in the database
                Optional<CreditCard> currentCreditCard = creditCardRepository.findByNumber(updateBalance.getCreditCardNumber());

                if (currentCreditCard.isEmpty()) {
                    //the given card number is not associated with a card, return 400 Bad Request
                    return ResponseEntity.badRequest().build();
                } else {
                    //get current balance history by the same date
                    CreditCard creditCard = currentCreditCard.get();
                    List<BalanceHistory> balanceHistoriesList = creditCard.getBalanceHistories();
                    Optional<BalanceHistory> currentBalanceHistory = creditCard.getBalanceHistories().stream().filter(history -> history.getDate().equals(updateBalance.getTransactionTime())).findFirst();

                    if (currentBalanceHistory.isEmpty()) {
                        // no balance history on the same date, add the balance history
                        BalanceHistory balanceHistory = new BalanceHistory(updateBalance.getTransactionTime(), updateBalance.getTransactionAmount(), creditCard);
                        balanceHistoriesList.add(balanceHistory);
                    } else {
                        //otherwise, update an existing one
                        currentBalanceHistory.get().setBalance(currentBalanceHistory.get().getBalance() + updateBalance.getTransactionAmount());
                    }

                    //update all balance history there is after update balance time
                   for (BalanceHistory balanceHistory : balanceHistoriesList) {
                        if (balanceHistory.getDate().isAfter(updateBalance.getTransactionTime())) {
                            balanceHistory.setBalance(balanceHistory.getBalance() + updateBalance.getTransactionAmount());
                        }
                    }

                    creditCard.setBalanceHistories(balanceHistoriesList);
                    //save credit card and balance histories in the database
                    creditCardRepository.save(creditCard);
                }
            }
            //return 200 OK if update is done and successful
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }
    
}
