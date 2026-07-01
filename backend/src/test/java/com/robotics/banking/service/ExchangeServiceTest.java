package com.robotics.banking.service;

import com.robotics.banking.config.ExchangeRateProperties;
import com.robotics.banking.dto.request.ExchangeRequest;
import com.robotics.banking.dto.response.ExchangeResponse;
import com.robotics.banking.entity.Account;
import com.robotics.banking.entity.User;
import com.robotics.banking.enums.Currency;
import com.robotics.banking.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@EnableConfigurationProperties(ExchangeRateProperties.class)
@Import({ExchangeService.class, AccountService.class, UserService.class})
class ExchangeServiceTest {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void exchangeMovesMoneyBetweenAccountsAndLinksLegs() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        Account eurAccount = new Account(user, Currency.EUR);
        eurAccount.setBalance(new BigDecimal("100.0000"));
        entityManager.persist(eurAccount);
        Account usdAccount = entityManager.persist(new Account(user, Currency.USD));
        entityManager.flush();

        ExchangeResponse response = exchangeService.exchange(
                new ExchangeRequest(eurAccount.getId(), usdAccount.getId(), new BigDecimal("100.00"), "rebalance"));

        assertThat(response.out().type()).isEqualTo(TransactionType.EXCHANGE_OUT);
        assertThat(response.out().amount()).isEqualByComparingTo("100.00");
        assertThat(response.out().balanceAfter()).isEqualByComparingTo("0.00");
        assertThat(response.in().type()).isEqualTo(TransactionType.EXCHANGE_IN);
        assertThat(response.in().amount()).isEqualByComparingTo("108.00");
        assertThat(response.in().balanceAfter()).isEqualByComparingTo("108.00");
        assertThat(response.in().counterpartyTransactionId()).isEqualTo(response.out().id());
        assertThat(response.out().counterpartyTransactionId()).isNull();

        assertThat(accountService.get(eurAccount.getId()).balance()).isEqualByComparingTo("0.00");
        assertThat(accountService.get(usdAccount.getId()).balance()).isEqualByComparingTo("108.00");
    }

    @Test
    void exchangeWithInsufficientFundsThrows409AndLeavesBalancesUnchanged() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        Account eurAccount = new Account(user, Currency.EUR);
        eurAccount.setBalance(new BigDecimal("10.0000"));
        entityManager.persist(eurAccount);
        Account usdAccount = entityManager.persist(new Account(user, Currency.USD));
        entityManager.flush();

        ExchangeRequest request = new ExchangeRequest(
                eurAccount.getId(), usdAccount.getId(), new BigDecimal("50.00"), null);

        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(accountService.get(eurAccount.getId()).balance()).isEqualByComparingTo("10.0000");
        assertThat(accountService.get(usdAccount.getId()).balance()).isEqualByComparingTo("0.0000");
    }

    @Test
    void exchangeToSameAccountThrows409() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        Account account = entityManager.persist(new Account(user, Currency.EUR));
        entityManager.flush();
        Long accountId = account.getId();

        ExchangeRequest request = new ExchangeRequest(accountId, accountId, new BigDecimal("10.00"), null);

        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void exchangeBetweenDifferentUsersAccountsThrows409() {
        User ada = entityManager.persist(new User("Ada", "ada@example.com"));
        User bob = entityManager.persist(new User("Bob", "bob@example.com"));
        Account adaAccount = new Account(ada, Currency.EUR);
        adaAccount.setBalance(new BigDecimal("100.0000"));
        entityManager.persist(adaAccount);
        Account bobAccount = entityManager.persist(new Account(bob, Currency.USD));
        entityManager.flush();

        ExchangeRequest request = new ExchangeRequest(
                adaAccount.getId(), bobAccount.getId(), new BigDecimal("10.00"), null);

        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void exchangeBetweenSameCurrencyAccountsMovesFullAmount() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        Account fromAccount = new Account(user, Currency.EUR);
        fromAccount.setBalance(new BigDecimal("50.0000"));
        entityManager.persist(fromAccount);
        Account toAccount = entityManager.persist(new Account(user, Currency.EUR));
        entityManager.flush();

        ExchangeResponse response = exchangeService.exchange(
                new ExchangeRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("20.00"), null));

        assertThat(response.out().amount()).isEqualByComparingTo("20.00");
        assertThat(response.in().amount()).isEqualByComparingTo("20.00");
    }
}
