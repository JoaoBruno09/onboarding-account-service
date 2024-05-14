package com.bank.onboarding.accountservice.services.impl;

import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.enums.CustomerType;
import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.persistence.repositories.AccountRepository;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.ACCOUNT_TYPES;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final OnboardingUtils onboardingUtils;

    @Override
    public List<Account> getAllAccounts() {
        return Optional.of(accountRepository.findAll()).orElse(Collections.emptyList());
    }

    @Override
    public AccountDTO patchAccountType(String accountNumber, AccountTypeRequestDTO accountTypeRequestDTO) throws OnboardingException {
        if(Boolean.TRUE.equals(Optional.ofNullable(accountTypeRequestDTO.getAccountActive()).orElse(Boolean.TRUE)) ||
                !ACCOUNT_TYPES.contains(Optional.ofNullable(accountTypeRequestDTO.getAccountType()).orElse("")))
            throw new OnboardingException("Não é possível adicionar/atualizar/remover tipo de conta. A conta está ativa ou o tipo de conta é inválido");

        Account account = onboardingUtils.findAccountDB(accountNumber);
        AccountDTO accountDTOReturned = null;
        String accountType = Optional.ofNullable(accountTypeRequestDTO.getAccountType()).orElse("");

        if(CustomerType.EMPRESA.name().equals(Optional.ofNullable(accountTypeRequestDTO.getCustomerType()).orElse("")) &&
                onboardingUtils.isEmpresaAccountType(accountType)) {
            accountDTOReturned = onboardingUtils.saveAccountTypeDB(account, accountType);
        }else if(CustomerType.PARTICULAR.name().equals(Optional.ofNullable(accountTypeRequestDTO.getCustomerType()).orElse(""))){
            int age = onboardingUtils.calculateAge(Optional.ofNullable(accountTypeRequestDTO.getCustomerBirthDate()).orElse(LocalDateTime.now()));
            String customerType = Optional.ofNullable(accountTypeRequestDTO.getCustomerType()).orElse("");

            if(onboardingUtils.isMajorAndUniversityStudentAndAccountTypeIsUNIV(age, accountTypeRequestDTO.getCustomerProfession(), customerType) ||
                    onboardingUtils.isMinorAndHasProgenitorOrTutorAndAccountTypeIsJOV(age, customerType) || onboardingUtils.isParticularAccountType(accountType)){
                accountDTOReturned = onboardingUtils.saveAccountTypeDB(account, accountType);
            }
        }

        return accountDTOReturned;
    }

    @Override
    public AccountDTO addAccountCard(String accountNumber, AccountCardDTO accountTypeDTO) {
        return null;
    }

    @Override
    public AccountDTO updateAccountCard(String accountNumber, String cardId, AccountCardDTO accountTypeDTO) {
        return null;
    }

    @Override
    public AccountDTO deleteAccountCard(String accountNumber, String cardId) {
        return null;
    }

    @Override
    public AccountDTO putAccountNetbanco(String accountNumber, AccountNetbancoDTO accountNetbancoDTO) {
        return null;
    }
}
