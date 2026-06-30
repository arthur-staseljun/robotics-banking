package com.robotics.banking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DebitRequest(
        @NotNull @Positive BigDecimal amount,
        String description) {
}
