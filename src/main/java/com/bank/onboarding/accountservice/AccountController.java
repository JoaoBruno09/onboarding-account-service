package com.bank.onboarding.accountservice;

import com.bank.onboarding.persistence.models.Account;
import com.bank.onboarding.persistence.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/")
    public List<Account> index() {
       return accountService.getAllAccounts();
    }
}
