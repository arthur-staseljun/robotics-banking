package com.robotics.banking.service;

import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.entity.Account;
import com.robotics.banking.entity.Transaction;
import com.robotics.banking.entity.User;
import com.robotics.banking.enums.Currency;
import com.robotics.banking.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({TransactionService.class, AccountService.class, UserService.class})
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void getReturnsTransaction() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        Account account = entityManager.persist(new Account(user, Currency.EUR));
        Transaction tx = entityManager.persist(new Transaction(account, TransactionType.DEPOSIT,
                new BigDecimal("100.0000"), Currency.EUR, new BigDecimal("100.0000"), "salary"));
        entityManager.flush();

        TransactionResponse response = transactionService.get(tx.getId());

        assertThat(response.id()).isEqualTo(tx.getId());
        assertThat(response.accountId()).isEqualTo(account.getId());
        assertThat(response.type()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void getFailsWith404WhenTransactionDoesNotExist() {
        assertThatThrownBy(() -> transactionService.get(999L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getHistoryReturnsOnlyThatAccountsTransactions() {
        Account ada = accountWithTransactions("ada@example.com", 2);
        accountWithTransactions("bob@example.com", 3);

        Page<TransactionResponse> page = transactionService.getHistory(ada.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(t -> t.accountId().equals(ada.getId()));
    }

    @Test
    void getHistoryRespectsPageSize() {
        Account ada = accountWithTransactions("ada@example.com", 3);

        Page<TransactionResponse> page = transactionService.getHistory(ada.getId(), PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void getHistoryFailsWith404WhenAccountDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        assertThatThrownBy(() -> transactionService.getHistory(999L, pageRequest))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Account accountWithTransactions(String email, int count) {
        User user = entityManager.persist(new User("User", email));
        Account account = entityManager.persist(new Account(user, Currency.EUR));
        for (int i = 1; i <= count; i++) {
            BigDecimal value = new BigDecimal(i + ".0000");
            entityManager.persist(new Transaction(account, TransactionType.DEPOSIT,
                    value, Currency.EUR, value, "tx" + i));
        }
        entityManager.flush();
        return account;
    }
}
