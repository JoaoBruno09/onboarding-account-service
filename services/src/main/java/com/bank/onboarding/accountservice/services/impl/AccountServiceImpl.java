package com.bank.onboarding.accountservice.services.impl;

import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.enums.CustomerType;
import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.persistence.models.Card;
import com.bank.onboarding.commonslib.persistence.repositories.AccountRepository;
import com.bank.onboarding.commonslib.persistence.repositories.CustomerRefRepository;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.ACCOUNT_TYPES;
import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.CARD_TYPES;
import static com.bank.onboarding.commonslib.persistence.enums.AccountPhase.INTYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRefRepository customerRefRepository;
    private final OnboardingUtils onboardingUtils;
    private final Faker faker = new Faker(new Locale("pt"));

    @Override
    public List<Account> getAllAccounts() {
        return Optional.of(accountRepository.findAll()).orElse(Collections.emptyList());
    }

    @Override
    public AccountDTO patchAccountType(String accountNumber, AccountTypeRequestDTO accountTypeRequestDTO) throws OnboardingException {
        if(Boolean.TRUE.equals(Optional.ofNullable(accountTypeRequestDTO.getAccountActive()).orElse(Boolean.TRUE)) ||
                !ACCOUNT_TYPES.contains(Optional.ofNullable(accountTypeRequestDTO.getAccountType()).orElse("")) ||
                !Objects.equals(INTYPE.getValue(), Optional.ofNullable(accountTypeRequestDTO.getAccountPhase()).orElse(0)))
            throw new OnboardingException("Não é possível adicionar/atualizar/remover tipo de conta. A conta está ativa ou o tipo de conta ou a fase introduzida são inválidos");

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
    public CardDTO addAccountCard(String accountNumber, AccountCardDTO accountCardDTO) {
        if(!CARD_TYPES.contains(Optional.ofNullable(accountCardDTO.getCardType()).orElse("")) ||
                !Objects.equals(INTYPE.getValue(), Optional.ofNullable(accountCardDTO.getAccountPhase()).orElse(0)))
            throw new OnboardingException("Não é possível adicionar cartão de conta. O cartão que está a tentar introduzir ou a fase introduzida são inválidos");

        Account account = onboardingUtils.findAccountDB(accountNumber);
        Card newCard = Card.builder()
                .annualFee(Double.valueOf(faker.commerce().price(5.00,20.00)))
                .cvc((int)faker.number().randomNumber(3, false))
                .number(faker.numerify("####-####-####-####"))
                .type(onboardingUtils.getCardTypeValue(accountCardDTO.getCardType()))
                .accountId(account.getId()).build();

        Optional.ofNullable(accountCardDTO.getCustomerNumber()).orElseThrow(() ->
                new OnboardingException("A lista de clientes para se adicionar o cartão está vazia")).forEach(customerNumber -> {
                    if(customerRefRepository.findByCustomerNumber(customerNumber).getId() != null) {
                        newCard.setCustomerId(customerNumber);
                        onboardingUtils.saveCardDB(newCard);
                    }else{
                        throw new OnboardingException("Não foi possível adicionar o cartão ao cliente com o número " + customerNumber);
                    }
        });

        return AccountMapper.INSTANCE.toCardDTO(newCard);
    }

    @Override
    public AccountDTO updateAccountCard(String accountNumber, String cardId, AccountCardDTO accountTypeDTO) {
        return null;
    }

    @Override
    public AccountDTO deleteAccountCard(String accountNumber, String cardNumber) {
        Card card = onboardingUtils.findCardDB(cardNumber);
        onboardingUtils.deleteCardDB(card.getId());
        Account account = onboardingUtils.findAccountDB(accountNumber);

        return AccountMapper.INSTANCE.toAccountDTO(account);
    }

    @Override
    public AccountDTO putAccountNetbanco(String accountNumber, AccountNetbancoDTO accountNetbancoDTO) {
        return null;
    }
}
