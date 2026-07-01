package com.robotics.banking.dto.response;

import com.robotics.banking.enums.Currency;

import java.math.BigDecimal;

public record AccountResponse(Long id, Long userId, Currency currency, BigDecimal balance) {
}
