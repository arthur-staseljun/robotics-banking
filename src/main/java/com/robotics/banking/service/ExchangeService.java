package com.robotics.banking.service;

import com.robotics.banking.config.ExchangeRateProperties;
import com.robotics.banking.dto.request.ExchangeRequest;
import com.robotics.banking.dto.response.ExchangeResponse;
import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.entity.Account;
import com.robotics.banking.entity.Transaction;
import com.robotics.banking.enums.Currency;
import com.robotics.banking.enums.TransactionType;
import com.robotics.banking.money.Money;
import com.robotics.banking.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.MathContext;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateProperties exchangeRateProperties;

    @Transactional
    public ExchangeResponse exchange(ExchangeRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot exchange into the same account");
        }

        Account source = accountService.getEntity(request.fromAccountId());
        Account destination = accountService.getEntity(request.toAccountId());
        if (!source.getUser().getId().equals(destination.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Accounts must belong to the same user");
        }

        BigDecimal sourceAmount = Money.normalize(request.amount());
        if (source.getBalance().compareTo(sourceAmount) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient funds in account " + request.fromAccountId());
        }
        BigDecimal destinationAmount = convert(sourceAmount, source.getCurrency(), destination.getCurrency());

        BigDecimal sourceBalanceAfter = source.getBalance().subtract(sourceAmount);
        source.setBalance(sourceBalanceAfter);
        BigDecimal destinationBalanceAfter = destination.getBalance().add(destinationAmount);
        destination.setBalance(destinationBalanceAfter);

        Transaction out = transactionRepository.save(new Transaction(source, TransactionType.EXCHANGE_OUT,
                sourceAmount, source.getCurrency(), sourceBalanceAfter, request.description()));

        Transaction in = new Transaction(destination, TransactionType.EXCHANGE_IN,
                destinationAmount, destination.getCurrency(), destinationBalanceAfter, request.description());
        in.setCounterpartyTransactionId(out.getId());
        in = transactionRepository.save(in);

        return new ExchangeResponse(toResponse(out), toResponse(in));
    }

    private BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) {
            return amount;
        }
        BigDecimal baseAmount = amount.divide(rate(from), MathContext.DECIMAL64);
        return Money.normalize(baseAmount.multiply(rate(to)));
    }

    private BigDecimal rate(Currency currency) {
        BigDecimal rate = exchangeRateProperties.rates().get(currency);
        if (rate == null) {
            throw new IllegalStateException("No exchange rate configured for currency " + currency);
        }
        return rate;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getAccount().getId(),
                transaction.getType(), transaction.getAmount(), transaction.getCurrency(),
                transaction.getBalanceAfter(), transaction.getDescription(), transaction.getCreatedAt(),
                transaction.getCounterpartyTransactionId());
    }
}
