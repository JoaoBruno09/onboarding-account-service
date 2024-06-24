package com.bank.onboarding.accountservice.services.impl;

import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.enums.CustomerType;
import com.bank.onboarding.commonslib.persistence.enums.OperationType;
import com.bank.onboarding.commonslib.persistence.enums.ValidationType;
import com.bank.onboarding.commonslib.persistence.exceptions.OnboardingException;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.persistence.models.Card;
import com.bank.onboarding.commonslib.persistence.models.CustomerRef;
import com.bank.onboarding.commonslib.persistence.services.AccountRepoService;
import com.bank.onboarding.commonslib.persistence.services.CardRepoService;
import com.bank.onboarding.commonslib.persistence.services.CustomerRefRepoService;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.utils.kafka.KafkaProducer;
import com.bank.onboarding.commonslib.utils.kafka.models.CardAndNetbancoEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.CreateAccountEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.ErrorEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.ValidationEvent;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
import com.bank.onboarding.commonslib.utils.mappers.CustomerMapper;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDeleteCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountRefDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.MoveNextPhaseDTO;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.ACCOUNT_PHASES;
import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.ACCOUNT_TYPES;
import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.CARD_TYPES;
import static com.bank.onboarding.commonslib.persistence.constants.OnboardingConstants.faker;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.ADD_INTERVENIENT;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.ADD_REL;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.CREATE_ACCOUNT;
import static com.bank.onboarding.commonslib.persistence.enums.OperationType.UPDATE_ACCOUNT_REF;
import static com.bank.onboarding.commonslib.persistence.enums.ValidationType.VALID_CARD;
import static com.bank.onboarding.commonslib.persistence.enums.ValidationType.VALID_CUSTOMERS;
import static com.bank.onboarding.commonslib.persistence.enums.ValidationType.VALID_DOCUMENTS;
import static com.bank.onboarding.commonslib.persistence.enums.ValidationType.VALID_NETBANCO;
import static com.bank.onboarding.commonslib.persistence.enums.ValidationType.VALID_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final CustomerRefRepoService customerRefRepoService;
    private final OnboardingUtils onboardingUtils;
    private final AccountRepoService accountRepoService;
    private final CardRepoService cardRepoService;
    private final KafkaProducer kafkaProducer;

    @Value("${spring.kafka.producer.customer.topic-name}")
    private String customerTopicName;

    @Value("${spring.kafka.producer.intervention.topic-name}")
    private String interventionTopicName;

    @Value("${spring.kafka.producer.document.topic-name}")
    private String documentTopicName;

    @Override
    public List<Account> getAllAccounts() {
        return accountRepoService.findAccountsDB();
    }

    @Override
    public AccountDTO createAccount(CreateAccountRequestDTO createAccountRequestDTO) {

        String accountManager = Optional.ofNullable(createAccountRequestDTO.getAccountManager()).orElse("");
        String accountType = Optional.ofNullable(createAccountRequestDTO.getAccountType()).orElse("");
        if(StringUtils.isBlank(accountManager) || !ACCOUNT_TYPES.contains(accountType))
            throw new OnboardingException("Não foi inserido um gestor de conta válido ou o tipo de conta é inválido, pelo que não é possível criar a conta.");

        String iban = faker.finance().iban("PT");

        Account account = accountRepoService.saveAccountDB(Account.builder()
            .accountManager(accountManager)
            .active(Boolean.FALSE)
            .creationTime(LocalDateTime.now())
            .currencyCode("EUR")
            .iban(iban)
            .lastUpdateTime(LocalDateTime.now())
            .number(iban.trim().substring(iban.length()-19))
            .onlineBankingIndicator(Boolean.FALSE)
            .phase(1)
            .type(accountType)
            .build());

        AccountRefDTO accountRefDTO = AccountRefDTO.builder().accountId(account.getId()).accountNumber(account.getNumber()).build();

        kafkaProducer.sendEvent(customerTopicName, CREATE_ACCOUNT, CreateAccountEvent.builder()
                .createAccountRequestDTO(createAccountRequestDTO)
                .accountRefDTO(accountRefDTO)
                .build());
        kafkaProducer.sendEvent(documentTopicName, UPDATE_ACCOUNT_REF , accountRefDTO);
        kafkaProducer.sendEvent(interventionTopicName, UPDATE_ACCOUNT_REF , accountRefDTO);

        return AccountMapper.INSTANCE.toAccountDTO(account);
    }

    @Override
    public AccountDTO patchAccountType(String accountNumber, AccountTypeRequestDTO accountTypeRequestDTO) throws OnboardingException {
        if(Boolean.TRUE.equals(accountTypeRequestDTO.isAccountActive()) ||
                !ACCOUNT_TYPES.contains(Optional.ofNullable(accountTypeRequestDTO.getAccountType()).orElse("")))
            throw new OnboardingException("Não é possível adicionar/atualizar/remover tipo de conta. A conta está ativa ou o tipo de conta são inválidos");

        onboardingUtils.isValidPhase(accountTypeRequestDTO.getAccountPhase(), OperationType.TYPE_ACCOUNT);

        Account account = accountRepoService.getAccountByNumber(accountNumber);
        AccountDTO accountDTOReturned = null;
        String accountType = Optional.ofNullable(accountTypeRequestDTO.getAccountType()).orElse("");

        if(CustomerType.EMPRESA.name().equals(Optional.ofNullable(accountTypeRequestDTO.getCustomerType()).orElse("")) &&
                onboardingUtils.isEmpresaAccountType(accountType)) {
            account.setType(accountType);
            account.setLastUpdateTime(LocalDateTime.now());
            Optional.ofNullable(account.getValidations()).orElse(new ArrayList<>()).add(VALID_TYPE);

            accountDTOReturned = AccountMapper.INSTANCE.toAccountDTO(accountRepoService.saveAccountDB(account));
        }else if(CustomerType.PARTICULAR.name().equals(Optional.ofNullable(accountTypeRequestDTO.getCustomerType()).orElse(""))){
            int age = onboardingUtils.calculateAge(Optional.ofNullable(accountTypeRequestDTO.getCustomerBirthDate()).orElse(LocalDateTime.now()));
            String customerType = Optional.ofNullable(accountTypeRequestDTO.getCustomerType()).orElse("");

            if(onboardingUtils.isMajorAndUniversityStudentAndAccountTypeIsUNIV(age, accountTypeRequestDTO.getCustomerProfession(), customerType) ||
                    onboardingUtils.isMinorAndHasProgenitorOrTutorAndAccountTypeIsJOV(age, customerType) || onboardingUtils.isParticularAccountType(accountType)){
                account.setType(accountType);
                account.setLastUpdateTime(LocalDateTime.now());
                Optional.ofNullable(account.getValidations()).orElse(new ArrayList<>()).add(VALID_TYPE);

                accountDTOReturned = AccountMapper.INSTANCE.toAccountDTO(accountRepoService.saveAccountDB(account));
            }
        }

        return accountDTOReturned;
    }

    @Override
    public CardDTO putAccountCard(String accountNumber, AccountCardDTO accountCardDTO) {
        if(!CARD_TYPES.contains(Optional.ofNullable(accountCardDTO.getCardType()).orElse("")))
            throw new OnboardingException("Não é possível adicionar cartão de conta. O cartão que está a tentar introduzir ou a fase introduzida são inválidos");

        onboardingUtils.isValidPhase(accountCardDTO.getAccountPhase(), OperationType.CARD_ACCOUNT);
        Account account = accountRepoService.getAccountByNumber(accountNumber);

        Card newCard = Card.builder()
                .annualFee(Double.valueOf(faker.commerce().price(5.00,20.00)))
                .cvc((int)faker.number().randomNumber(3, false))
                .number(faker.numerify("####-####-####-####"))
                .type(cardRepoService.getCardTypeValue(accountCardDTO.getCardType()))
                .accountId(account.getId()).build();

        Optional.ofNullable(accountCardDTO.getCustomerNumber()).orElseThrow(() ->
                new OnboardingException("A lista de clientes para se adicionar o cartão está vazia")).forEach(customerNumber -> {
                    CustomerRef customerRefDTO = customerRefRepoService.findCustomerRefByCustomerNumber(customerNumber);
                    String customerId = customerRefDTO.getId();

                    if(customerId != null) {
                        cardRepoService.findAndDeleteCardDB(customerId, account.getId());
                        newCard.setCustomerId(customerNumber);
                        cardRepoService.saveCardDB(newCard);

                        Optional.ofNullable(account.getValidations()).orElse(new ArrayList<>()).add(VALID_CARD);
                        accountRepoService.saveAccountDB(account);

                        kafkaProducer.sendEvent(customerTopicName, OperationType.CARD_ACCOUNT,
                                CardAndNetbancoEvent.builder().value(true).customerRefDTO(CustomerMapper.INSTANCE.toCustomerRefDTO(customerRefDTO)).build());
                    }else{
                        throw new OnboardingException("Não foi possível adicionar o cartão ao cliente com o número " + customerNumber);
                    }
        });

        return AccountMapper.INSTANCE.toCardDTO(newCard);
    }

    @Override
    public AccountDTO deleteAccountCard(String accountNumber, String cardNumber, AccountDeleteCardDTO accountDeleteCardDTO) {
        onboardingUtils.isValidPhase(accountDeleteCardDTO.getAccountPhase(), OperationType.CARD_ACCOUNT);
        CustomerRef customerRefDTO = customerRefRepoService.findCustomerRefByCustomerNumber(accountDeleteCardDTO.getCustomerNumber());
        if (customerRefDTO.getId() != null){
            Card card = cardRepoService.findCardDB(cardNumber);
            cardRepoService.deleteCardDB(card.getId());

            Account account = accountRepoService.getAccountById(card.getAccountId());
            Optional.ofNullable(account.getValidations()).ifPresent(validationTypes ->
                account.setValidations(validationTypes.stream().filter(validationType -> !VALID_CARD.equals(validationType)).toList()));
            accountRepoService.saveAccountDB(account);

            kafkaProducer.sendEvent(customerTopicName, OperationType.CARD_ACCOUNT,
                    CardAndNetbancoEvent.builder().value(false).customerRefDTO(CustomerMapper.INSTANCE.toCustomerRefDTO(customerRefDTO)).build());
        }

        return AccountMapper.INSTANCE.toAccountDTO(accountRepoService.getAccountByNumber(accountNumber));
    }

    @Override
    public AccountDTO putAccountNetbanco(String accountNumber, AccountNetbancoDTO accountNetbancoDTO) {
        onboardingUtils.isValidPhase(accountNetbancoDTO.getAccountPhase(), OperationType.NETBANCO_ACCOUNT);
        CustomerRef customerRefDTO = customerRefRepoService.findCustomerRefByCustomerNumber(accountNetbancoDTO.getCustomerNumber());
        Account account = accountRepoService.getAccountByNumber(accountNumber);

        if (customerRefDTO.getId() != null){
            boolean wantsNetbanco = accountNetbancoDTO.isWantsNetbanco();
            account.setOnlineBankingIndicator(wantsNetbanco);
            account.setLastUpdateTime(LocalDateTime.now());

            if(wantsNetbanco){
                Optional.ofNullable(account.getValidations()).orElse(new ArrayList<>()).add(VALID_NETBANCO);
            }else{
                AtomicReference<List<ValidationType>> validations = new AtomicReference<>(new ArrayList<>());
                Optional.ofNullable(account.getValidations()).ifPresent(validationTypes ->
                        validations.set(validationTypes.stream().filter(validationType ->
                                !VALID_NETBANCO.equals(validationType)).collect(Collectors.toList())));

                if(!validations.get().isEmpty()) account.setValidations(validations.get());
            }

            account = accountRepoService.saveAccountDB(account);

            kafkaProducer.sendEvent(customerTopicName, OperationType.NETBANCO_ACCOUNT,
                    CardAndNetbancoEvent.builder().value(wantsNetbanco).customerRefDTO(CustomerMapper.INSTANCE.toCustomerRefDTO(customerRefDTO)).build());
        }

        return AccountMapper.INSTANCE.toAccountDTO(account);
    }

    @Override
    public void handleErrorEvent(ErrorEvent errorEvent) {
        if(CREATE_ACCOUNT.equals(errorEvent.getOperationType())){
            accountRepoService.deleteAccountById(errorEvent.getAccountRefDTO().getAccountId());
        }else if (Boolean.TRUE.equals(errorEvent.getIsNewCustomer())
                && (ADD_INTERVENIENT.equals(errorEvent.getOperationType()) || ADD_REL.equals(errorEvent.getOperationType()))){
            customerRefRepoService.deleteCustomerById(errorEvent.getCustomerRefDTO().getCustomerId());
        }
    }

    @Override
    public AccountDTO moveToNextPhase(String accountNumber, MoveNextPhaseDTO moveNextPhaseDTO) {
        int nextPhase =  moveNextPhaseDTO.getNextPhase();
        if(!ACCOUNT_PHASES.contains(nextPhase))
            throw new OnboardingException("Não é possível avançar de fase. A fase seguinte introduzida não é válida");

        Account account = accountRepoService.getAccountByNumber(accountNumber);

        switch (nextPhase) {
            case 2 -> {
                if (!account.getValidations().contains(VALID_TYPE))
                    onboardingUtils.throwInvalidPhaseForOperationTypeException();
            }
            case 3 -> {
                if (!new HashSet<>(account.getValidations()).containsAll(List.of(VALID_TYPE, VALID_CUSTOMERS, VALID_CARD, VALID_NETBANCO)))
                    onboardingUtils.throwInvalidPhaseForOperationTypeException();
            }
            case 4 -> {
                if (!new HashSet<>(account.getValidations()).containsAll(List.of(VALID_TYPE, VALID_CUSTOMERS, VALID_CARD, VALID_NETBANCO, VALID_DOCUMENTS)))
                    onboardingUtils.throwInvalidPhaseForOperationTypeException();
            }
            default -> {}
        }

        account.setPhase(nextPhase);

        return AccountMapper.INSTANCE.toAccountDTO(account);
    }

    @Override
    public void validateAccount(ValidationEvent validationEvent) {
        Account accountToBeUpdated = accountRepoService.getAccountById(validationEvent.getAccountId());
        if(accountToBeUpdated.getValidations() == null) accountToBeUpdated.setValidations(new ArrayList<>());
        accountToBeUpdated.getValidations().add(validationEvent.getValidationType());
        accountRepoService.saveAccountDB(accountToBeUpdated);
    }
}
