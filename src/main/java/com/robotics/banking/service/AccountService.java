package com.robotics.banking.service;

import com.robotics.banking.dto.request.CreateAccountRequest;
import com.robotics.banking.dto.request.DebitRequest;
import com.robotics.banking.dto.request.DepositRequest;
import com.robotics.banking.dto.response.AccountResponse;
import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.entity.Account;
import com.robotics.banking.entity.Transaction;
import com.robotics.banking.enums.TransactionType;
import com.robotics.banking.entity.User;
import com.robotics.banking.persistence.AccountRepository;
import com.robotics.banking.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int MONEY_SCALE = 4;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_EVEN;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional
    public AccountResponse create(Long userId, CreateAccountRequest request) {
        User user = userService.getEntity(userId);
        Account account = new Account(user, request.currency());
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listByUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse deposit(Long accountId, DepositRequest request) {
        Account account = getEntity(accountId);
        BigDecimal amount = request.amount().setScale(MONEY_SCALE, MONEY_ROUNDING);
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(account, TransactionType.DEPOSIT,
                amount, account.getCurrency(), newBalance, request.description());
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse debit(Long accountId, DebitRequest request) {
        Account account = getEntity(accountId);
        BigDecimal amount = request.amount().setScale(MONEY_SCALE, MONEY_ROUNDING);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient funds in account " + accountId);
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(account, TransactionType.DEBIT,
                amount, account.getCurrency(), newBalance, request.description());
        return toResponse(transactionRepository.save(transaction));
    }

    Account getEntity(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account " + id + " not found"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getUser().getId(),
                account.getCurrency(), account.getBalance());
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getAccount().getId(),
                transaction.getType(), transaction.getAmount(), transaction.getCurrency(),
                transaction.getBalanceAfter(), transaction.getDescription(), transaction.getCreatedAt());
    }
}
