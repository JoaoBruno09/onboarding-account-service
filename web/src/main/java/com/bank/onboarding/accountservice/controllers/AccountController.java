package com.bank.onboarding.accountservice.controllers;


import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.persistence.models.Card;
import com.bank.onboarding.commonslib.persistence.services.CardService;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CardService cardService;

    private static final String ACCOUNT_ID_PATH_PARAM = "/{accountNumber}";

    @GetMapping("/test/accounts")
    public List<Account> getAccounts() {
       return accountService.getAllAccounts();
    }

    @GetMapping("/test/cards")
    public List<Card> getCards() {
        return cardService.getAllCards();
    }

    @PatchMapping(ACCOUNT_ID_PATH_PARAM)
    public ResponseEntity<AccountDTO> patchAccountType(@PathVariable("accountNumber") String accountNumber,
                                                       @RequestBody AccountTypeRequestDTO accountTypeRequestDTO){
        try {
            final AccountDTO accountDTO = accountService.patchAccountType(accountNumber, accountTypeRequestDTO);
            if(accountDTO == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inseridos inválidos");
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( OnboardingException e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(ACCOUNT_ID_PATH_PARAM + "/card")
    public ResponseEntity<AccountDTO> addAccountCard(@PathVariable("accountNumber") String accountNumber,
                                                     @RequestBody AccountCardDTO accountCardDTO){
        try {
            final AccountDTO accountDTO = accountService.addAccountCard(accountNumber, accountCardDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.CREATED);
        }
        catch( Exception e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(ACCOUNT_ID_PATH_PARAM + "/card/{cardId}")
    public ResponseEntity<AccountDTO> updateAccountCard(@PathVariable("accountNumber") String accountNumber,
                                                        @PathVariable("cardId") String cardId,
                                                        @RequestBody AccountCardDTO accountCardDTO){
        try {
            final AccountDTO accountDTO = accountService.updateAccountCard(accountNumber, cardId, accountCardDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(ACCOUNT_ID_PATH_PARAM + "/card/{cardId}")
    public ResponseEntity<AccountDTO> deleteAccountCard(@PathVariable("accountNumber") String accountNumber,
                                                        @PathVariable("cardId") String cardId){
        try {
            final AccountDTO accountDTO = accountService.deleteAccountCard(accountNumber, cardId);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(ACCOUNT_ID_PATH_PARAM + "/netbanco")
    public ResponseEntity<AccountDTO> putAccountNetbanco(@PathVariable("accountNumber") String accountNumber,
                                                         @RequestBody AccountNetbancoDTO accountNetbancoDTO){
        try {
            final AccountDTO accountDTO = accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
