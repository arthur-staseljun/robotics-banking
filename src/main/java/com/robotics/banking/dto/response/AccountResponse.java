package com.robotics.banking.dto.response;

import com.robotics.banking.model.Currency;

import java.math.BigDecimal;

public record AccountResponse(Long id, Long userId, Currency currency, BigDecimal balance) {
}
