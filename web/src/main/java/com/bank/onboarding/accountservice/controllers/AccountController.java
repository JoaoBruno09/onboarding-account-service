package com.bank.onboarding.accountservice.controllers;


import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDeleteCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private static final String ACCOUNT_ID_PATH_PARAM = "/{accountNumber}";

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody @Valid CreateAccountRequestDTO createAccountRequestDTO){
        try {
            final AccountDTO accountDTO = accountService.createAccount(createAccountRequestDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch(OnboardingException e ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping(ACCOUNT_ID_PATH_PARAM)
    public ResponseEntity<AccountDTO> patchAccountType(@PathVariable("accountNumber") String accountNumber,
                                                       @RequestBody @Valid AccountTypeRequestDTO accountTypeRequestDTO){
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
    public ResponseEntity<CardDTO> putAccountCard(@PathVariable("accountNumber") String accountNumber,
                                                  @RequestBody @Valid AccountCardDTO accountCardDTO){
        try {
            final CardDTO cardDTO = accountService.putAccountCard(accountNumber, accountCardDTO);
            return new ResponseEntity<>(cardDTO, HttpStatus.CREATED);
        }
        catch( Exception e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(ACCOUNT_ID_PATH_PARAM + "/card/{cardNumber}")
    public ResponseEntity<AccountDTO> deleteAccountCard(@PathVariable("accountNumber") String accountNumber,
                                                        @PathVariable("cardNumber") String cardNumber,
                                                        @RequestBody @Valid AccountDeleteCardDTO accountDeleteCardDTO){
        try {
            final AccountDTO accountDTO = accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(ACCOUNT_ID_PATH_PARAM + "/netbanco")
    public ResponseEntity<AccountDTO> putAccountNetbanco(@PathVariable("accountNumber") String accountNumber,
                                                         @RequestBody @Valid AccountNetbancoDTO accountNetbancoDTO){
        try {
            final AccountDTO accountDTO = accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
