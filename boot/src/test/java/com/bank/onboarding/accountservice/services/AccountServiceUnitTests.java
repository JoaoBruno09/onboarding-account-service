package com.bank.onboarding.accountservice.services;

import com.bank.onboarding.accountservice.Application;
import com.bank.onboarding.commonslib.persistence.enums.AccountPhase;
import com.bank.onboarding.commonslib.persistence.enums.AccountType;
import com.bank.onboarding.commonslib.persistence.enums.CardType;
import com.bank.onboarding.commonslib.persistence.models.Account;
import com.bank.onboarding.commonslib.utils.mappers.AccountMapper;
import com.bank.onboarding.commonslib.web.dtos.account.AccountCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountDeleteCardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountNetbancoDTO;
import com.bank.onboarding.commonslib.web.dtos.account.AccountTypeRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CardDTO;
import com.bank.onboarding.commonslib.web.dtos.account.CreateAccountRequestDTO;
import com.bank.onboarding.commonslib.web.dtos.account.MoveNextPhaseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccount;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountCardDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountNetbancoDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildAccountTypeRequestDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildCardDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildCreateAccountRequestDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildMoveNextPhaseDTO;
import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.deleteAccountCardDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = Application.class)
@ExtendWith({SpringExtension.class})
class AccountServiceUnitTests {

    @Mock
    private AccountService accountService;

    private AccountDTO accountDTO;
    private String accountNumber;

    @BeforeEach
    public void setUp() {
        Account account = buildAccount();
        accountDTO = AccountMapper.INSTANCE.toAccountDTO(account);
        accountNumber = account.getNumber();
    }

    @Test
    void createAccountTest() throws Exception{
        CreateAccountRequestDTO createAccountRequestDTO = buildCreateAccountRequestDTO();

        when(accountService.createAccount(createAccountRequestDTO)).thenReturn(accountDTO);

        assertEquals("Mário Ferreira Neves", accountService.createAccount(createAccountRequestDTO).getAccountManager());
        assertEquals("EUR", accountService.createAccount(createAccountRequestDTO).getCurrencyCode());
        assertEquals("PT50 0000 2927 8040 8012 4082 5", accountService.createAccount(createAccountRequestDTO).getIban());
        assertEquals("8040801240825", accountService.createAccount(createAccountRequestDTO).getNumber());
        assertEquals(AccountPhase.INTYPE.getValue(), accountService.createAccount(createAccountRequestDTO).getPhase());
        assertEquals(AccountType.ORDEM.name(), accountService.createAccount(createAccountRequestDTO).getType());
    }

    @Test
    void patchAccountTypeTest() throws Exception{
        AccountTypeRequestDTO accountTypeRequestDTO = buildAccountTypeRequestDTO();

        when(accountService.patchAccountType(accountNumber, accountTypeRequestDTO)).thenReturn(accountDTO);

        assertEquals("Mário Ferreira Neves", accountService.patchAccountType(accountNumber, accountTypeRequestDTO).getAccountManager());
        assertEquals("EUR", accountService.patchAccountType(accountNumber, accountTypeRequestDTO).getCurrencyCode());
        assertEquals("PT50 0000 2927 8040 8012 4082 5", accountService.patchAccountType(accountNumber, accountTypeRequestDTO).getIban());
        assertEquals("8040801240825", accountService.patchAccountType(accountNumber, accountTypeRequestDTO).getNumber());
        assertEquals(AccountPhase.INTYPE.getValue(),  accountService.patchAccountType(accountNumber, accountTypeRequestDTO).getPhase());
        assertEquals(AccountType.ORDEM.name(),  accountService.patchAccountType(accountNumber, accountTypeRequestDTO).getType());
    }

    @Test
    void putAccountCardTest() throws Exception{
        AccountCardDTO accountCardDTO = buildAccountCardDTO();
        CardDTO expectedCardDTO = buildCardDTO();

        when(accountService.putAccountCard(accountNumber, accountCardDTO)).thenReturn(expectedCardDTO);

        assertEquals(10, accountService.putAccountCard(accountNumber, accountCardDTO).getAnnualFee());
        assertEquals(111, accountService.putAccountCard(accountNumber, accountCardDTO).getCvc());
        assertEquals("1234-5678-9101-1121", accountService.putAccountCard(accountNumber, accountCardDTO).getNumber());
        assertEquals(CardType.CD.name(), accountService.putAccountCard(accountNumber, accountCardDTO).getType());
    }

    @Test
    void deleteAccountCardTest() throws Exception{
        String cardNumber = "12345";
        AccountDeleteCardDTO accountDeleteCardDTO = deleteAccountCardDTO();

        when(accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO)).thenReturn(accountDTO);

        assertEquals("Mário Ferreira Neves", accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO).getAccountManager());
        assertEquals("EUR", accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO).getCurrencyCode());
        assertEquals("PT50 0000 2927 8040 8012 4082 5", accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO).getIban());
        assertEquals("8040801240825", accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO).getNumber());
        assertEquals(AccountPhase.INTYPE.getValue(), accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO).getPhase());
        assertEquals(AccountType.ORDEM.name(), accountService.deleteAccountCard(accountNumber, cardNumber, accountDeleteCardDTO).getType());
    }

    @Test
    void putAccountNetbancoTest() throws Exception{
        AccountNetbancoDTO accountNetbancoDTO = buildAccountNetbancoDTO();

        when(accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO)).thenReturn(accountDTO);

        assertEquals("Mário Ferreira Neves", accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO).getAccountManager());
        assertEquals("EUR", accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO).getCurrencyCode());
        assertEquals("PT50 0000 2927 8040 8012 4082 5", accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO).getIban());
        assertEquals("8040801240825", accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO).getNumber());
        assertEquals(AccountPhase.INTYPE.getValue(), accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO).getPhase());
        assertEquals(AccountType.ORDEM.name(), accountService.putAccountNetbanco(accountNumber, accountNetbancoDTO).getType());
    }

    @Test
    void putMoveNextPhaseTest() throws Exception{
        MoveNextPhaseDTO moveNextPhaseDTO = buildMoveNextPhaseDTO();

        when(accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO)).thenReturn(accountDTO);

        assertEquals("Mário Ferreira Neves", accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO).getAccountManager());
        assertEquals("EUR", accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO).getCurrencyCode());
        assertEquals("PT50 0000 2927 8040 8012 4082 5", accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO).getIban());
        assertEquals("8040801240825", accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO).getNumber());
        assertEquals(AccountPhase.INTYPE.getValue(), accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO).getPhase());
        assertEquals(AccountType.ORDEM.name(), accountService.moveToNextPhase(accountNumber, moveNextPhaseDTO).getType());
    }
}
