package com.bank.onboarding.accountservice.services;

import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.utils.kafka.ErrorEvent;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDeleteCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;

import java.util.List;

public interface AccountService {
    List<Account> getAllAccounts();
    AccountDTO createAccount(CreateAccountRequestDTO createAccountRequestDTO);
    AccountDTO patchAccountType(String accountNumber, AccountTypeRequestDTO accountTypeRequestDTO) throws OnboardingException;
    CardDTO putAccountCard(String accountNumber, AccountCardDTO accountTypeDTO);
    AccountDTO deleteAccountCard(String accountNumber, String cardId, AccountDeleteCardDTO accountDeleteCardDTO);
    AccountDTO putAccountNetbanco(String accountNumber, AccountNetbancoDTO accountNetbancoDTO);
    void handleErrorEvent(ErrorEvent errorEvent);
}
