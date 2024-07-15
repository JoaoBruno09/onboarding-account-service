package com.bank.onboarding.accountservice.controllers;

import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.enums.CardType;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.persistence.repositories.AccountRepository;
import com.bank.onboarding.commonslib.persistence.repositories.AddressRepository;
import com.bank.onboarding.commonslib.persistence.repositories.CardRepository;
import com.bank.onboarding.commonslib.persistence.repositories.ContactRepository;
import com.bank.onboarding.commonslib.persistence.repositories.CustomerRefRepository;
import com.bank.onboarding.commonslib.persistence.repositories.CustomerRepository;
import com.bank.onboarding.commonslib.persistence.repositories.DocumentRepository;
import com.bank.onboarding.commonslib.persistence.repositories.InterventionRepository;
import com.bank.onboarding.commonslib.persistence.repositories.RelationRepository;
import com.bank.onboarding.commonslib.persistence.services.AccountRepoService;
import com.bank.onboarding.commonslib.persistence.services.CardRepoService;
import com.bank.onboarding.commonslib.persistence.services.CustomerRefRepoService;
import com.bank.onboarding.commonslib.utils.OnboardingUtils;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
import com.bank.onboarding.commonslib.web.SecurityConfig;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDeleteCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.MoveNextPhaseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccount;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountCardDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountNetbancoDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountTypeRequestDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildCardDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildCreateAccountRequestDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildMoveNextPhaseDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.deleteAccountCardDTO;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
@MockBeans({
        @MockBean(OnboardingUtils.class),
        @MockBean(AccountRepoService.class),
        @MockBean(CardRepoService.class),
        @MockBean(CustomerRefRepoService.class),
        @MockBean(CustomerRefRepository.class),
        @MockBean(CustomerRepository.class),
        @MockBean(AccountRepository.class),
        @MockBean(CardRepository.class),
        @MockBean(DocumentRepository.class),
        @MockBean(InterventionRepository.class),
        @MockBean(RelationRepository.class),
        @MockBean(ContactRepository.class),
        @MockBean(AddressRepository.class)
})
class AccountControllerUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private AccountService accountService;

    @Value("${bank.onboarding.client.id}")
    private String clientId;

    private AccountDTO accountDTO;
    private String accountNumber;
    private String token;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        token = securityConfig.generateJWToken();
        objectMapper.registerModule(new JavaTimeModule());

        Account account = buildAccount();
        accountDTO = AccountMapper.INSTANCE.toAccountDTO(account);
        accountNumber = account.getNumber();
    }

    @Test
    void createAccountTest() throws Exception{
        CreateAccountRequestDTO createAccountRequestDTO = buildCreateAccountRequestDTO();

        when(accountService.createAccount(createAccountRequestDTO)).thenReturn(accountDTO);
        mockMvc.perform(post("/accounts")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Onboarding-Client-Id", clientId)
                        .content(objectMapper.writeValueAsString(createAccountRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.accountManager").value(accountDTO.getAccountManager()))
                .andExpect(jsonPath("$.currencyCode").value(accountDTO.getCurrencyCode()))
                .andExpect(jsonPath("$.iban").value(accountDTO.getIban()))
                .andExpect(jsonPath("$.number").value(accountNumber))
                .andExpect(jsonPath("$.onlineBankingIndicator").value(accountDTO.getOnlineBankingIndicator()))
                .andExpect(jsonPath("$.phase").value(accountDTO.getPhase()))
                .andExpect(jsonPath("$.type").value(accountDTO.getType()));
    }

    @Test
    void patchAccountTypeTest() throws Exception{
        AccountTypeRequestDTO accountTypeRequestDTO = buildAccountTypeRequestDTO();

        when(accountService.patchAccountType(accountNumber, accountTypeRequestDTO)).thenReturn(accountDTO);
        mockMvc.perform(put("/accounts/"+ accountNumber)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Onboarding-Client-Id", clientId)
                        .content(objectMapper.writeValueAsString(accountTypeRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.accountManager").value(accountDTO.getAccountManager()))
                .andExpect(jsonPath("$.currencyCode").value(accountDTO.getCurrencyCode()))
                .andExpect(jsonPath("$.iban").value(accountDTO.getIban()))
                .andExpect(jsonPath("$.number").value(accountNumber))
                .andExpect(jsonPath("$.onlineBankingIndicator").value(accountDTO.getOnlineBankingIndicator()))
                .andExpect(jsonPath("$.phase").value(accountDTO.getPhase()))
                .andExpect(jsonPath("$.type").value(accountDTO.getType()));
    }

    @Test
    void putAccountCardTest() throws Exception{
        AccountCardDTO accountCardDTO = buildAccountCardDTO();
        CardDTO expectedCardDTO = buildCardDTO();

        when(accountService.putAccountCard(accountNumber, accountCardDTO)).thenReturn(expectedCardDTO);
        mockMvc.perform(put("/accounts/"+ accountNumber + "/card")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Onboarding-Client-Id", clientId)
                        .content(objectMapper.writeValueAsString(accountCardDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.annualFee").value(10))
                .andExpect(jsonPath("$.cvc").value(111))
                .andExpect(jsonPath("$.number").value("1234-5678-9101-1121"))
                .andExpect(jsonPath("$.type").value(CardType.CD.name()));
    }

    @Test
    void deleteAccountCardTest() throws Exception{
        String cardNumber = "12345";
        AccountDeleteCardDTO accountDeleteCardDTO = deleteAccountCardDTO();

        when(accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO)).thenReturn(accountDTO);
        mockMvc.perform(delete("/accounts/"+ accountNumber +"/card/" + cardNumber)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Onboarding-Client-Id", clientId)
                        .content(objectMapper.writeValueAsString(accountDeleteCardDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.accountManager").value(accountDTO.getAccountManager()))
                .andExpect(jsonPath("$.currencyCode").value(accountDTO.getCurrencyCode()))
                .andExpect(jsonPath("$.iban").value(accountDTO.getIban()))
                .andExpect(jsonPath("$.number").value(accountNumber))
                .andExpect(jsonPath("$.onlineBankingIndicator").value(accountDTO.getOnlineBankingIndicator()))
                .andExpect(jsonPath("$.phase").value(accountDTO.getPhase()))
                .andExpect(jsonPath("$.type").value(accountDTO.getType()));
    }

    @Test
    void putAccountNetbancoTest() throws Exception{
        AccountNetbancoDTO accountNetbancoDTO = buildAccountNetbancoDTO();

        when(accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO)).thenReturn(accountDTO);
        mockMvc.perform(put("/accounts/"+ accountNumber +"/netbanco")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Onboarding-Client-Id", clientId)
                        .content(objectMapper.writeValueAsString(accountNetbancoDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.accountManager").value(accountDTO.getAccountManager()))
                .andExpect(jsonPath("$.currencyCode").value(accountDTO.getCurrencyCode()))
                .andExpect(jsonPath("$.iban").value(accountDTO.getIban()))
                .andExpect(jsonPath("$.number").value(accountNumber))
                .andExpect(jsonPath("$.onlineBankingIndicator").value(accountDTO.getOnlineBankingIndicator()))
                .andExpect(jsonPath("$.phase").value(accountDTO.getPhase()))
                .andExpect(jsonPath("$.type").value(accountDTO.getType()));
    }

    @Test
    void putMoveNextPhaseTest() throws Exception{
        MoveNextPhaseDTO moveNextPhaseDTO = buildMoveNextPhaseDTO();

        when(accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO)).thenReturn(accountDTO);
        mockMvc.perform(put("/accounts/"+ accountNumber +"/moveNextPhase")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Onboarding-Client-Id", clientId)
                        .content(objectMapper.writeValueAsString(moveNextPhaseDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.accountManager").value(accountDTO.getAccountManager()))
                .andExpect(jsonPath("$.currencyCode").value(accountDTO.getCurrencyCode()))
                .andExpect(jsonPath("$.iban").value(accountDTO.getIban()))
                .andExpect(jsonPath("$.number").value(accountNumber))
                .andExpect(jsonPath("$.onlineBankingIndicator").value(accountDTO.getOnlineBankingIndicator()))
                .andExpect(jsonPath("$.phase").value(accountDTO.getPhase()))
                .andExpect(jsonPath("$.type").value(accountDTO.getType()));
    }
}
