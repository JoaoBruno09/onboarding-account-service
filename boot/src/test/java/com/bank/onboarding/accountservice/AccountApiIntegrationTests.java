package com.bank.onboarding.accountservice;

import com.bank.onboarding.accountservice.services.AccountService;
import com.bank.onboarding.commonslib.persistence.enums.AccountPhase;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.persistence.models.Card;
import com.bank.onboarding.commonslib.persistence.repositories.AccountRepository;
import com.bank.onboarding.commonslib.persistence.repositories.CardRepository;
import com.bank.onboarding.commonslib.web.SecurityConfig;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccount;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountCardDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountNetbancoDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountTypeRequestDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildCard;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildCreateAccountRequestDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildMoveNextPhaseDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.deleteAccountCardDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountApiIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SecurityConfig securityConfig;

    @Value("${bank.onboarding.client.id}")
    private String clientId;

    private String token;
    private Account account;

    private HttpHeaders httpHeaders;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        token = securityConfig.generateJWToken();
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(token);
        httpHeaders.set("X-Onboarding-Client-Id", clientId);
        objectMapper.registerModule(new JavaTimeModule());

        account = buildAccount();
        accountRepository.save(account);
    }

    @AfterEach
    public void setDown(){
        accountRepository.deleteByNumber(account.getNumber());
        cardRepository.findAllByAccountId(account.getId()).forEach(card -> cardRepository.deleteById(card.getId()));
    }

    private String createURLWithPort() {
        return "http://localhost:" + port + "/accounts";
    }

    @Test
    void createAccountTest() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(buildCreateAccountRequestDTO()), httpHeaders);
        ResponseEntity<?> response = restTemplate.exchange(
                createURLWithPort(), HttpMethod.POST, entity, AccountDTO.class);

        AccountDTO accountDTO = (AccountDTO) response.getBody();
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(201));
        assert accountDTO != null;
        assertEquals(accountDTO.getType(), accountService.createAccount(buildCreateAccountRequestDTO()).getType());
        assertEquals(accountDTO.getType(), accountRepository.findByNumber(accountDTO.getNumber()).getType());
    }

    @Test
    void patchAccountTypeTest() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(buildAccountTypeRequestDTO()), httpHeaders);
        ResponseEntity<?> response = restTemplate.exchange(
                createURLWithPort() + "/" + account.getNumber(), HttpMethod.PUT, entity, AccountDTO.class);

        AccountDTO accountDTO = (AccountDTO) response.getBody();
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
        assert accountDTO != null;
        assertEquals(accountDTO.getType(), accountService.patchAccountType(account.getNumber(), buildAccountTypeRequestDTO()).getType());
        assertEquals(accountDTO.getType(), accountRepository.findByNumber(account.getNumber()).getType());
    }

    @Test
    void putAccountCardTest() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(buildAccountCardDTO()), httpHeaders);
        ResponseEntity<?> response = restTemplate.exchange(
                createURLWithPort() + "/" + account.getNumber() + "/card", HttpMethod.PUT, entity, CardDTO.class);

        CardDTO cardDTO = (CardDTO) response.getBody();
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
        assert cardDTO != null;
        assertEquals(cardDTO.getType(), accountService.putAccountCard(account.getNumber(), buildAccountCardDTO()).getType());
    }

    @Test
    void deleteAccountCardTest() throws JsonProcessingException {
        Card card = cardRepository.save(buildCard(account.getId()));
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(deleteAccountCardDTO()), httpHeaders);
        ResponseEntity<?> response = restTemplate.exchange(
                createURLWithPort() + "/" + account.getNumber() + "/card/" + card.getNumber(), HttpMethod.DELETE, entity, AccountDTO.class);

        AccountDTO accountDTO = (AccountDTO) response.getBody();
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
        assert accountDTO != null;
        assertNull(cardRepository.findByNumber(card.getNumber()));
        card = cardRepository.save(buildCard(account.getId()));
        assertEquals(accountDTO, accountService.deleteAccountCard(account.getNumber(), card.getNumber(),deleteAccountCardDTO()));
    }

    @Test
    void putAccountNetbancoTest() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(buildAccountNetbancoDTO()), httpHeaders);
        ResponseEntity<?> response = restTemplate.exchange(
                createURLWithPort() + "/" + account.getNumber() + "/netbanco", HttpMethod.PUT, entity, AccountDTO.class);

        AccountDTO accountDTO = (AccountDTO) response.getBody();
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
        assert accountDTO != null;
        assertTrue(accountDTO.getOnlineBankingIndicator());
        assertTrue(accountService.putAccountNetbanco(account.getNumber(), buildAccountNetbancoDTO()).getOnlineBankingIndicator());
        assertTrue(accountRepository.findByNumber(account.getNumber()).getOnlineBankingIndicator());
    }

    @Test
    void putMoveNextPhaseTest() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(buildMoveNextPhaseDTO()), httpHeaders);
        ResponseEntity<?> response = restTemplate.exchange(
                createURLWithPort() + "/" + account.getNumber() + "/moveNextPhase", HttpMethod.PUT, entity, AccountDTO.class);

        AccountDTO accountDTO = (AccountDTO) response.getBody();
        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
        assert accountDTO != null;
        assertEquals(AccountPhase.RELCARD.getValue(), accountDTO.getPhase());
        assertEquals(AccountPhase.RELCARD.getValue(), accountRepository.findByNumber(account.getNumber()).getPhase());
        assertEquals(AccountPhase.RELCARD.getValue(),accountService.moveToNextPhase(account.getNumber(), buildMoveNextPhaseDTO()).getPhase());
    }
}
