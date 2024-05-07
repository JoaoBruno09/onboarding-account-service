package com.bank.onboarding.accountservice.controllers;


import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.persistence.models.Card;
import com.bank.onboarding.commonslib.persistence.services.AccountService;
import com.bank.onboarding.commonslib.persistence.services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CardService cardService;

    @GetMapping("/test/accounts")
    public List<Account> getAccounts() {
       return accountService.getAllAccounts();
    }

    @GetMapping("/test/cards")
    public List<Card> getCards() {
        return cardService.getAllCards();
    }
}
