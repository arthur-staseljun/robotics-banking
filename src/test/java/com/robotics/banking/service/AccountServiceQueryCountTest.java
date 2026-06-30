package com.robotics.banking.service;

import com.robotics.banking.dto.response.AccountResponse;
import com.robotics.banking.entity.Account;
import com.robotics.banking.enums.Currency;
import com.robotics.banking.entity.User;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AccountService.class, UserService.class})
class AccountServiceQueryCountTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void listByUserRunsExactlyOneQueryRegardlessOfAccountCount() {
        User user = entityManager.persist(new User("Ada", "ada@example.com"));
        entityManager.persist(new Account(user, Currency.EUR));
        entityManager.persist(new Account(user, Currency.USD));
        entityManager.persist(new Account(user, Currency.GBP));

        entityManager.flush();
        entityManager.clear();

        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        List<AccountResponse> accounts = accountService.listByUser(user.getId());

        assertThat(accounts).hasSize(3);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }
}
