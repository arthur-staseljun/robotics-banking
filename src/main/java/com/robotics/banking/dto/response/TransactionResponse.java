package com.robotics.banking.dto.response;

import com.robotics.banking.enums.Currency;
import com.robotics.banking.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long accountId,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt) {
}
