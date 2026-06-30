package com.robotics.banking.service;

import com.robotics.banking.dto.request.CreateAccountRequest;
import com.robotics.banking.dto.request.DepositRequest;
import com.robotics.banking.dto.response.AccountResponse;
import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.entity.Account;
import com.robotics.banking.enums.Currency;
import com.robotics.banking.enums.TransactionType;
import com.robotics.banking.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({AccountService.class, UserService.class})
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void createLinksUserAndStartsAtZeroBalance() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));

        AccountResponse response = accountService.create(user.getId(), new CreateAccountRequest(Currency.EUR));

        assertThat(response.id()).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.currency()).isEqualTo(Currency.EUR);
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createFailsWith404WhenUserDoesNotExist() {
        var createAccountRequest = new CreateAccountRequest(Currency.EUR);
        assertThatThrownBy(() -> accountService.create(999L, createAccountRequest))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getFailsWith404WhenAccountDoesNotExist() {
        assertThatThrownBy(() -> accountService.get(999L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listByUserReturnsOnlyThatUsersAccounts() {
        User ada = entityManager.persist(new User("Ada", "ada@example.com"));
        User bob = entityManager.persist(new User("Bob", "bob@example.com"));
        entityManager.persist(new Account(ada, Currency.EUR));
        entityManager.persist(new Account(ada, Currency.USD));
        entityManager.persist(new Account(bob, Currency.GBP));
        entityManager.flush();

        List<AccountResponse> adaAccounts = accountService.listByUser(ada.getId());

        assertThat(adaAccounts)
                .hasSize(2)
                .allMatch(accountResponse -> accountResponse.userId().equals(ada.getId()));
        assertThat(adaAccounts).extracting(AccountResponse::currency)
                .containsExactlyInAnyOrder(Currency.EUR, Currency.USD);
    }

    @Test
    void depositIncreasesBalanceAndWritesLedgerEntry() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        Account account = entityManager.persist(new Account(user, Currency.EUR));
        entityManager.flush();

        TransactionResponse response = accountService.deposit(
                account.getId(), new DepositRequest(new BigDecimal("100.50"), "salary"));

        assertThat(response.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.accountId()).isEqualTo(account.getId());
        assertThat(response.amount()).isEqualByComparingTo("100.50");
        assertThat(response.currency()).isEqualTo(Currency.EUR);
        assertThat(response.balanceAfter()).isEqualByComparingTo("100.50");
        assertThat(response.description()).isEqualTo("salary");

        assertThat(accountService.get(account.getId()).balance()).isEqualByComparingTo("100.50");
    }

    @Test
    void successiveDepositsAccumulateBalance() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        Account account = entityManager.persist(new Account(user, Currency.EUR));
        entityManager.flush();

        accountService.deposit(account.getId(), new DepositRequest(new BigDecimal("40.00"), null));
        TransactionResponse second = accountService.deposit(
                account.getId(), new DepositRequest(new BigDecimal("60.00"), null));

        assertThat(second.balanceAfter()).isEqualByComparingTo("100.00");
        assertThat(accountService.get(account.getId()).balance()).isEqualByComparingTo("100.00");
    }
}
