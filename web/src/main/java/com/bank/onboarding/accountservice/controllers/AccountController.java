package com.bank.onboarding.accountservice.controllers;


import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDeleteCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.MoveNextPhaseDTO;
import feign.Request;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final OnboardingUtils onboardingUtils;

    private static final String ACCOUNT_NUMBER_PATH_PARAM = "/{accountNumber}";

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAccount(@RequestBody @Valid CreateAccountRequestDTO createAccountRequestDTO,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                           @RequestHeader("X-Onboarding-Client-Id") String clientId) {
        try {
            final AccountDTO accountDTO = accountService.createAccount(createAccountRequestDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.CREATED);
        }
        catch(OnboardingException e ) {
            return onboardingUtils.buildResponseEntity(Request.HttpMethod.POST.name(), e.getMessage());
        }
    }

    @PutMapping(value = ACCOUNT_NUMBER_PATH_PARAM, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> patchAccountType(@PathVariable("accountNumber") String accountNumber,
                                                       @RequestBody @Valid AccountTypeRequestDTO accountTypeRequestDTO,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                       @RequestHeader("X-Onboarding-Client-Id") String clientId){
        try {
            final AccountDTO accountDTO = accountService.patchAccountType(accountNumber, accountTypeRequestDTO);
            if(accountDTO == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inseridos inválidos");
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( OnboardingException e ) {
            return onboardingUtils.buildResponseEntity(Request.HttpMethod.PATCH.name(), e.getMessage());
        }
    }

    @PutMapping(value = ACCOUNT_NUMBER_PATH_PARAM + "/card", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putAccountCard(@PathVariable("accountNumber") String accountNumber,
                                                  @RequestBody @Valid AccountCardDTO accountCardDTO,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestHeader("X-Onboarding-Client-Id") String clientId){
        try {
            final CardDTO cardDTO = accountService.putAccountCard(accountNumber, accountCardDTO);
            return new ResponseEntity<>(cardDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            return onboardingUtils.buildResponseEntity(Request.HttpMethod.PUT.name(), e.getMessage());
        }
    }

    @DeleteMapping(value = ACCOUNT_NUMBER_PATH_PARAM + "/card/{cardNumber}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAccountCard(@PathVariable("accountNumber") String accountNumber,
                                                        @PathVariable("cardNumber") String cardNumber,
                                                        @RequestBody @Valid AccountDeleteCardDTO accountDeleteCardDTO,
                                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                        @RequestHeader("X-Onboarding-Client-Id") String clientId){
        try {
            final AccountDTO accountDTO = accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            return onboardingUtils.buildResponseEntity(Request.HttpMethod.DELETE.name(), e.getMessage());
        }
    }

    @PutMapping(value = ACCOUNT_NUMBER_PATH_PARAM + "/netbanco", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putAccountNetbanco(@PathVariable("accountNumber") String accountNumber,
                                                         @RequestBody @Valid AccountNetbancoDTO accountNetbancoDTO,
                                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                         @RequestHeader("X-Onboarding-Client-Id") String clientId){
        try {
            final AccountDTO accountDTO = accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            return onboardingUtils.buildResponseEntity(Request.HttpMethod.PUT.name(), e.getMessage());
        }
    }

    @PutMapping(value = ACCOUNT_NUMBER_PATH_PARAM + "/moveNextPhase", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putMoveNextPhase(@PathVariable("accountNumber") String accountNumber,
                                                @RequestBody @Valid MoveNextPhaseDTO moveNextPhaseDTO,
                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                @RequestHeader("X-Onboarding-Client-Id") String clientId){
        try {
            final AccountDTO accountDTO = accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO);
            return new ResponseEntity<>(accountDTO, HttpStatus.OK);
        }
        catch( Exception e ) {
            return onboardingUtils.buildResponseEntity(Request.HttpMethod.PUT.name(), e.getMessage());
        }
    }

}